package com.freestudy.api.event;

import com.freestudy.api.BaseControllerTest;
import com.freestudy.api.DisplayName;
import com.freestudy.api.event.location.Location;
import com.freestudy.api.event.type.EventType;
import com.freestudy.api.event.type.EventTypeDto;
import com.freestudy.api.interest.InterestDto;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.swing.event.HyperlinkEvent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

  @Test
  public void createEventTest() throws Exception {
    EventType eventType1 = createEventType("밥 같이 먹어요");
    EventType eventType2 = createEventType("조금 떠들어요");
    Set<EventTypeDto> eventTypes = new HashSet<>(Arrays.asList(eventType1.toDto(), eventType2.toDto()));
    // Given
    EventDto event = EventDto.builder()
            .name("낙성대 주말 코딩")
            .description("오전 10시부터 오후 3시까지 각자 모여서 코딩합니다.")
            .startedAt(LocalDateTime.of(2018, 11, 11, 12, 0))
            .endedAt(LocalDateTime.of(2018, 11, 11, 14, 0))
            .limitOfEnrollment(5)
            .location(new Location("남부순환로", "스타벅스"))
            .eventTypes(eventTypes)
            .build();

    // When
    var perform = mockMvc
            .perform(
                    post("/api/events/")
                            .header(HttpHeaders.AUTHORIZATION, getAuthToken())
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .content(objectMapper.writeValueAsString(event))
            );

    // Then
    perform
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").isNumber())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(
                    document("post-events",
                            links(
                                    linkWithRel("self").description("link to self"),
                                    linkWithRel("query-events").description("query")
                            ),
                            requestHeaders(
                                    headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("모임 이름"),
                                    fieldWithPath("description").description("모임 설명"),
                                    fieldWithPath("location.address").description("모임 장소 주소"),
                                    fieldWithPath("location.name").description("모임 장소 이름"),
                                    fieldWithPath("eventTypes[].id").description("모임 성격 id"),
                                    fieldWithPath("eventTypes[].value").description("모임 성격 값"),
                                    fieldWithPath("startedAt").description("모임 시작 시간"),
                                    fieldWithPath("endedAt").description("모임 종료 시간"),
                                    fieldWithPath("limitOfEnrollment").description("모임 정원")
                            ),
                            relaxedResponseFields(
                                    fieldWithPath("id").description("event id")
                            )
                    )
            );

  }

  @Test
  @DisplayName("입력값이 없는 경우에")
  public void createEventTest__empty_input() throws Exception {
    // Given
    EventDto eventDto = EventDto.builder().build();

    // When
    var perform = mockMvc
            .perform(
                    post("/api/events")
                            .header(HttpHeaders.AUTHORIZATION, getAuthToken())
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(objectMapper.writeValueAsString(eventDto))
            );

    // Then
    perform
            .andDo(document("post-events-invalid"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("_links.index").exists());
  }

  @Test
  @DisplayName("이벤트 시작일은 종료일보다 이전이여야 한다.")
  public void createEventTest_invalid_input() throws Exception {
    // Given
    EventDto eventDto = EventDto.builder()
            .name("SpringBootIsFun")
            .description("Rest")
            .startedAt(LocalDateTime.of(2018, 11, 15, 0, 0))
            .endedAt(LocalDateTime.of(2018, 11, 11, 0, 0))
            .limitOfEnrollment(5)
            .location(new Location("남부순환로", "낙성대"))
            .build();

    // When
    var perform = mockMvc
            .perform(
                    post("/api/events")
                            .header(HttpHeaders.AUTHORIZATION, getAuthToken())
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .accept(MediaTypes.HAL_JSON)
                            .content(objectMapper.writeValueAsString(eventDto))
            );

    // Then
    perform
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("content[0].objectName").exists())
            .andExpect(jsonPath("content[0].defaultMessage").exists())
            .andExpect(jsonPath("_links.index").exists());
  }


  @Test
  @DisplayName("30개의 이벤트를 페이징 조회")
  public void queryEvents__happy() throws Exception {
    // Given
    IntStream.range(0, 30).forEach(__ -> this.createEvent());

    // When
    var perform = this.mockMvc.perform(
            get("/api/events")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .param("page", "1")
                    .param("size", "2")
                    .param("sort", "name,desc")
    );

    // Then
    perform
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("content[0].id").exists())
            .andDo(
                    document("query-events",
                            requestParameters(
                                    parameterWithName("page").description("페이지"),
                                    parameterWithName("size").description("페이지의 크기"),
                                    parameterWithName("sort").description("<:field>,<:sort> 형태. 값을 URL encoding 해야한다. 예시 참고")
                            )
                    ));
  }

  @Test
  @DisplayName("기존 이벤트 하나 조회")
  public void getEvent() throws Exception {
    // Given
    Event event = createEvent();

    // When
    var perform = this.mockMvc.perform(get("/api/events/{id}", event.getId()));

    // Then
    perform.andExpect(status().isOk());
    perform.andExpect(jsonPath("id").exists());
    perform.andExpect(jsonPath("name").exists());
    perform.andExpect(jsonPath("description").exists());
    perform.andExpect(jsonPath("_links.self").exists());
    perform.andExpect(jsonPath("_links.profile").exists());
    perform.andDo(
            document("get-event",
                    pathParameters(
                            parameterWithName("id").description("event id")

                    )
            ));
  }


  @Test
  @DisplayName("기존 이벤트 하나 조회, 이벤트가 없을 때")
  public void getEvent__not_found() throws Exception {
    // When
    var perform = this.mockMvc.perform(get("/api/events/{id}", 0));

    // Then
    perform.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("이벤트 수정")
  public void updateEvent__happy() throws Exception {
    // Given
    Event event = createEvent();
    String updatedName = "updated event";
    EventDto eventDto = EventDto
            .builder()
            .name(updatedName)
            .location(event.getLocation())
            .description(event.getDescription())
            .startedAt(event.getStartedAt())
            .endedAt(event.getEndedAt())
            .limitOfEnrollment(event.getLimitOfEnrollment())
            .build();

    // When
    var perform = this.mockMvc.perform(
            put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, getAuthToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto))
    );

    perform.andDo(print());
    perform.andDo(document("events-update"));
    perform.andExpect(status().isOk());
    perform.andExpect(jsonPath("name").value(updatedName));
  }

  @Test
  @DisplayName("이벤트 수정, 없는 이벤트에 대해서")
  public void updateEvent__not_found() throws Exception {
    // Given
    Event event = createEvent();
    EventDto eventDto = this.modelMapper.map(event, EventDto.class);
    int eventIdNotExists = -1;

    // When
    var perform = this.mockMvc.perform(
            put("/api/events/{id}", eventIdNotExists)
                    .header(HttpHeaders.AUTHORIZATION, getAuthToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto))
    );

    perform.andDo(print());
    perform.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("이벤트 수정, 시작시간을 종료시간 이후의 값을 넣는 경우")
  public void updateEvent__invalid_startedAt_endedAt() throws Exception {
    // Given
    Event event = createEvent();
    EventDto eventDto = EventDto.builder()
            .startedAt(LocalDateTime.of(2018, 11, 16, 0, 0))
            .endedAt(LocalDateTime.of(2018, 11, 15, 0, 0))
            .build();

    // When
    var perform = this.mockMvc.perform(
            put("/api/events/{id}", event.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto))
                    .header(HttpHeaders.AUTHORIZATION, getAuthToken())
    );

    // Then
    perform.andDo(print());
    perform.andExpect(status().isBadRequest());
  }

}
