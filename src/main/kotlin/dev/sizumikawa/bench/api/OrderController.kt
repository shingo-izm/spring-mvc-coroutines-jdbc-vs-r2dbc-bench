package dev.sizumikawa.bench.api

import dev.sizumikawa.bench.domain.ListOrdersRequest
import dev.sizumikawa.bench.domain.OrderDto
import dev.sizumikawa.bench.domain.OrderService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class OrderController(
    private val service: OrderService
) {
    @GetMapping("/orders")
    suspend fun listOrders(
        @RequestParam customerId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): List<OrderDto> {
        val req = ListOrdersRequest(
            customerId = customerId,
            from = from,
            to = to,
            page = page,
            size = size.coerceIn(1, 200)
        )
        return service.listOrders(req)
    }
}
