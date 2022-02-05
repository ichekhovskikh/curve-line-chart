package com.zero.chartview.extensions

internal inline val Any?.isNull get() = this == null

internal fun <T : Any?> T.isEqualsOrNull(other: T) =
    this.isNull || this == other

internal fun <T : Any> T.takeIfNull(other: T?): T? =
    takeIf { other.isNull }
