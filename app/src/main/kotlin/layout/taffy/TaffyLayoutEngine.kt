package com.foam.app.layout.taffy

import com.foam.app.core.node.Node
import com.foam.app.layout.LayoutEngine
import dev.vfyjxf.taffy.geometry.TaffySize
import dev.vfyjxf.taffy.style.AlignContent
import dev.vfyjxf.taffy.style.AlignItems
import dev.vfyjxf.taffy.style.AvailableSpace
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.LengthPercentage
import dev.vfyjxf.taffy.style.TaffyDimension
import dev.vfyjxf.taffy.style.TaffyDisplay
import dev.vfyjxf.taffy.style.TaffyStyle
import dev.vfyjxf.taffy.tree.TaffyTree

class TaffyLayoutEngine : LayoutEngine {
    override fun layout(root: Node, width: Float, height: Float) {
        val tree = TaffyTree()
        val childNodes = root.children.map {
            child ->
            val style = child.computedStyle
            val taffyStyle = TaffyStyle().apply {
                size = TaffySize(
                    TaffyDimension.length(style.width ?: 0f),
                    TaffyDimension.length(style.height ?: 0f)
                )
            }
            child to tree.newLeaf(
                taffyStyle
            )
        }
        val rootStyle =
            TaffyStyle().apply {

                display =
                    TaffyDisplay.FLEX

                flexDirection =
                    FlexDirection.ROW

                size =
                    TaffySize(
                        TaffyDimension.length(width),
                        TaffyDimension.length(height)
                    )

                gap =
                    TaffySize(
                        LengthPercentage.length(
                            root.computedStyle.gap
                        ),
                        LengthPercentage.length(
                            0f
                        )
                    )

                justifyContent =
                    AlignContent.CENTER

                alignItems =
                    AlignItems.CENTER
            }
        val rootNode = tree.newWithChildren(
            rootStyle,
            *childNodes
                .map { it.second }
                .toTypedArray()
        )
        tree.computeLayout(
            rootNode,
            TaffySize(
                AvailableSpace.definite(width),
                AvailableSpace.definite(height)
            )
        )
        for ((foamNode, taffyNode) in childNodes) {
            val layout = tree.getLayout(taffyNode)
            foamNode.layout.x = layout.location().x
            foamNode.layout.y = layout.location().y
            foamNode.layout.width = layout.size().width
            foamNode.layout.height = layout.size().height
        }

    }

}
