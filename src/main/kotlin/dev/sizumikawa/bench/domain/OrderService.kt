package dev.sizumikawa.bench.domain

import org.springframework.stereotype.Service

@Service
class OrderService(
    private val repository: OrderRepository
) {
    suspend fun listOrders(req: ListOrdersRequest): List<OrderDto> = repository.findOrders(req)
}
