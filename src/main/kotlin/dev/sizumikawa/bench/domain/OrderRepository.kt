package dev.sizumikawa.bench.domain

import java.math.BigDecimal
import java.time.Instant

interface OrderRepository {
    suspend fun findOrders(req: ListOrdersRequest): List<OrderDto>
}

data class OrderDto(
    val id: Long,
    val customerId: Long,
    val createdAt: Instant,
    val totalAmount: BigDecimal,
    val note: String?
)

data class ListOrdersRequest(
    val customerId: Long,
    val from: Instant,
    val to: Instant,
    val page: Int = 0,
    val size: Int = 50
) {
    val offset: Int get() = page * size
}
