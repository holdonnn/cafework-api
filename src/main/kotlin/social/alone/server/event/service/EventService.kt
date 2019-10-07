package social.alone.server.event.service

import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import social.alone.server.event.domain.Event
import social.alone.server.event.dto.EventDto
import social.alone.server.event.repository.EventRepository
import social.alone.server.event.type.EventTypeRepository
import social.alone.server.location.Location
import social.alone.server.location.LocationRepository
import social.alone.server.user.repository.UserRepository
import java.util.*

@Slf4j
@Service
@Transactional
class EventService(
        val eventRepository: EventRepository,
        val userRepository: UserRepository,
        val eventTypeRepository: EventTypeRepository,
        val locationRepository: LocationRepository
) {


    fun update(event: Event, eventDto: EventDto): Event {
        val location = getLocation(eventDto)
        updateEventTypes(event, eventDto)
        event.updateLocation(location)
        event.updateByEventDto(eventDto)
        return this.eventRepository.save(event)
    }

    private fun updateEventTypes(event: Event, eventDto: EventDto) {
        val eventTypes = eventTypeRepository
                .findAllById(eventDto.eventTypes.map { i -> i.id})
        event.eventTypes = HashSet(eventTypes)
    }

    private fun getLocation(eventDto: EventDto): Location {
        val location = eventDto.getLocation()
        val by = locationRepository
                .findByLongitudeAndLatitudeAndName(
                        location.longitude!!,
                        location.latitude!!,
                        location.name
                )
        return by.orElseGet { locationRepository.save(location) }
    }
}
