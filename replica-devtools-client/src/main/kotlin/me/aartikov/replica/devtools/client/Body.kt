package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaClientDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Ul

@Composable
fun Body(state: ReplicaClientDto) {
    Div(
        attrs = {
            style {
                position(Position.Absolute)
                width(100.percent)
                height(100.percent)
                top(0.px)
                bottom(0.px)
                left(0.px)
                right(0.px)
                property("margin", auto)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    width(100.percent)
                    height(100.percent)
                    position(Position.Absolute)
                    top(0.px)
                    left(0.px)
                }
            }
        ) {
            Content(state)
        }
    }
}

@Composable
fun Content(state: ReplicaClientDto) {
    Div(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
                overflowY("scroll")
            }
        }
    ) {
        Ul(
            attrs = {
                style {
                    width(100.percent)
                    margin(0.px)
                }
            }
        ) {
            state.replicas.values.forEach { replica ->
                ReplicaItem(item = replica)
            }
            state.keyedReplicas.values.forEach { replica ->
                KeyedReplicaItem(item = replica)
            }
        }
    }
}