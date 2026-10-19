package com.cocktailcraft.format

import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.delivery_estimate_minutes
import com.cocktailcraft.resources.delivery_estimate_processing_soon
import com.cocktailcraft.resources.order_status_cancelled
import com.cocktailcraft.resources.order_status_delivered
import com.cocktailcraft.resources.order_status_pending
import com.cocktailcraft.resources.order_status_processing
import com.cocktailcraft.resources.order_status_shipped
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

/** Localized label for the order list. */
val OrderStatus.displayName: StringDesc
    get() = when (this) {
        OrderStatus.PENDING -> MR.strings.order_status_pending
        OrderStatus.PROCESSING -> MR.strings.order_status_processing
        OrderStatus.SHIPPED -> MR.strings.order_status_shipped
        OrderStatus.DELIVERED -> MR.strings.order_status_delivered
        OrderStatus.CANCELLED -> MR.strings.order_status_cancelled
    }.desc()

/** Estimated delivery time for an order in this status. */
val OrderStatus.deliveryEstimate: StringDesc
    get() = when (this) {
        OrderStatus.PENDING -> MR.strings.delivery_estimate_processing_soon.desc()
        OrderStatus.PROCESSING -> minutes(from = 15, to = 25)
        OrderStatus.SHIPPED -> minutes(from = 5, to = 10)
        OrderStatus.DELIVERED -> MR.strings.order_status_delivered.desc()
        OrderStatus.CANCELLED -> MR.strings.order_status_cancelled.desc()
    }

/** [deliveryEstimate] for an order that may not be there ("Unknown"). */
internal fun deliveryEstimateFor(status: OrderStatus?): StringDesc =
    status?.deliveryEstimate ?: MR.strings.common_unknown.desc()

private fun minutes(from: Int, to: Int): StringDesc = MR.strings.delivery_estimate_minutes.format(from, to)
