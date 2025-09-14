package dev.sizumikawa.bench.infra.jdbc

import dev.sizumikawa.bench.domain.ListOrdersRequest
import dev.sizumikawa.bench.domain.OrderDto
import dev.sizumikawa.bench.domain.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.sql.Connection
import javax.sql.DataSource

@Repository
@Profile("jdbc")
class JdbcOrderRepository(
    private val dataSource: DataSource
) : OrderRepository {

    override suspend fun findOrders(req: ListOrdersRequest): List<OrderDto> =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { conn ->
                query(conn, req)
            }
        }

    private fun query(conn: Connection, req: ListOrdersRequest): List<OrderDto> {
        val sql = """
        SELECT id, customer_id, created_at, total_amount, note
        FROM orders
        WHERE customer_id = ? AND created_at BETWEEN ? AND ?
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    """.trimIndent()

        return conn.prepareStatement(sql).use { ps ->
            ps.setLong(1, req.customerId)
            ps.setObject(2, req.from)
            ps.setObject(3, req.to)
            ps.setInt(4, req.size)
            ps.setInt(5, req.offset)
            ps.executeQuery().use { rs ->
                val list = ArrayList<OrderDto>()
                while (rs.next()) {
                    list += OrderDto(
                        id = rs.getLong(1),
                        customerId = rs.getLong(2),
                        createdAt = rs.getTimestamp(3).toInstant(),
                        totalAmount = rs.getBigDecimal(4),
                        note = rs.getString(5)
                    )
                }
                list
            }
        }
    }
}
