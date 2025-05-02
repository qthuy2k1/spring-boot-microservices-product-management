package com.qthuy2k1.orderservice.repository;


import com.qthuy2k1.orderservice.model.ProductReportList;
import com.qthuy2k1.orderservice.model.ReportModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderReportRepository {

    @Autowired
    private EntityManager entityManager;

    public ReportModel getOrderReport(String startDate, String endDate) {
        Query query = entityManager.createNativeQuery("""
                WITH FilteredOrders AS (
                    SELECT *
                    FROM orders_tbl
                    WHERE created_at BETWEEN to_timestamp(:startDate, 'YYYY-MM-DD HH24:MI:SS') AND to_timestamp(:endDate, 'YYYY-MM-DD HH24:MI:SS')
                ),
                TotalCustomers AS (
                    SELECT DISTINCT ON (user_id)
                        id,
                        user_id,
                        created_at,
                        CASE
                            WHEN created_at = (SELECT MIN(created_at) FROM orders_tbl WHERE user_id = o.user_id)
                            THEN 'New Customer'
                            ELSE 'Returning Customer'
                        END AS customer_type
                    FROM orders_tbl o
                )
                SELECT
                    COALESCE(CAST(:endDate AS DATE) - CAST(:startDate AS DATE), 0) AS period,
                    COALESCE(COUNT(*), 0) AS totalOrders,
                    COALESCE(AVG(total_amount), 0) AS avgOrderValue,
                    COALESCE((SELECT COUNT(*) FROM TotalCustomers tc WHERE tc.customer_type = 'New Customer'), 0) AS newCustomers,
                    COALESCE((SELECT COUNT(*) FROM TotalCustomers tc WHERE tc.customer_type = 'Returning Customer'), 0) AS returningCustomers,
                    COALESCE((SELECT COUNT(*) FROM FilteredOrders WHERE status = 'PENDING'), 0) AS pending,
                    COALESCE((SELECT COUNT(*) FROM FilteredOrders WHERE status = 'PROCESSING'), 0) AS processing,
                    COALESCE((SELECT COUNT(*) FROM FilteredOrders WHERE status = 'SHIPPED'), 0) AS shipped,
                    COALESCE((SELECT COUNT(*) FROM FilteredOrders WHERE status = 'DELIVERED'), 0) AS delivered,
                    COALESCE((SELECT COUNT(*) FROM FilteredOrders WHERE status = 'CANCELED'), 0) AS canceled
                FROM FilteredOrders
                """);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        return (ReportModel) query.unwrap(org.hibernate.query.NativeQuery.class)
                .setResultTransformer(Transformers.aliasToBean(ReportModel.class))
                .getSingleResult();
    }

    public List<ProductReportList> getProductReportList() {
        Query query = entityManager.createNativeQuery("""
                select
                	oit.product_id,
                	count(oit.product_id) as product_count
                from
                	order_items_tbl oit
                group by
                	oit.product_id
                order by
                	product_count desc
                limit 5;
                """);

        return (List<ProductReportList>) query.unwrap(org.hibernate.query.NativeQuery.class)
                .setResultTransformer(Transformers.aliasToBean(ProductReportList.class))
                .getResultList();
    }
}