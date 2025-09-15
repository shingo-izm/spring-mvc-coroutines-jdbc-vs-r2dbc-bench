package dev.sizumikawa.bench.infra.r2dbc

import dev.sizumikawa.bench.domain.ListOrdersRequest
import dev.sizumikawa.bench.domain.OrderDto
import dev.sizumikawa.bench.domain.OrderRepository
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant

@Repository
@Profile("r2dbc")
class R2dbcOrderRepository(
    private val client: DatabaseClient
) : OrderRepository {

    override suspend fun findOrders(req: ListOrdersRequest): List<OrderDto> {
        val sql = """
            SELECT id, customer_id, created_at, total_amount, note
            FROM orders
            WHERE customer_id = :cid AND created_at BETWEEN :from AND :to
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        return client.sql(sql)
            .bind("cid", req.customerId)
            .bind("from", req.from)
            .bind("to", req.to)
            .bind("limit", req.size)
            .bind("offset", req.offset)
            .map { row ->
                OrderDto(
                    id = row.get("id", Long::class.java)!!.toLong(),
                    customerId = row.get("customer_id", Long::class.java)!!.toLong(),
                    createdAt = row.get("created_at", Instant::class.java)!!,
                    totalAmount = row.get("total_amount", BigDecimal::class.java)!!,
                    note = row.get("note", String::class.java)
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }
}
