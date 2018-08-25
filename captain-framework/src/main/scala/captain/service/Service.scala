package captain.service

import captain.message.MessagingService

/**
 * Service trait is the core interface to implement a single microservice.
 *
 * While the implementation is getting initialized:
 *
 * 1. Initiate a messaging infrastructure.
 */
trait Service extends MessagingService
