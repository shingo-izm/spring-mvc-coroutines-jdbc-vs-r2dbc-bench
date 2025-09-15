package dev.sizumikawa.bench.infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Profile("jdbc")
class SeedRunner(
    dataSource: DataSource,
    @Value("\${bench.seed.enabled:true}") private val enabled: Boolean,
    @Value("\${bench.seed.rows:200000}") private val rows: Int
) : org.springframework.boot.ApplicationRunner {
    private val jdbc = JdbcTemplate(dataSource)

    override fun run(args: org.springframework.boot.ApplicationArguments) {
        if (!enabled) {
            println("Seeding is disabled.")
            return
        }

        val count = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long::class.java) ?: 0L
        if (count > 0) {
            println("Orders table is not empty (count=$count). Skipping seeding.")
            return
        }

        // ランダム分布: customer_id(1..5000), created_at(過去365日), amount(0..1000)
        val sql = """
            INSERT INTO orders (customer_id, created_at, total_amount, note)
            SELECT
              (1 + floor(random() * 5000))::bigint,
              NOW() - (floor(random() * 365) || ' days')::interval,
              round((random() * 1000)::numeric, 2),
              'note-' || gs
            FROM generate_series(1, ?) AS gs;
        """.trimIndent()

        runBlocking {
            withContext(Dispatchers.IO) {
                jdbc.update(sql, rows)
            }
        }
        // 軽い統計
        val after = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long::class.java)
        println("Seeded rows: $after")
    }
}
