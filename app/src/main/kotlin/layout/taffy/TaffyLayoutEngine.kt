package com.foam.app.layout.taffy

import com.foam.app.core.node.Node
import com.foam.app.core.node.TextNode
import com.foam.app.layout.LayoutEngine
import com.foam.app.style.flex.AlignItems as FoamAlignItems
import com.foam.app.style.flex.Display
import com.foam.app.style.flex.FlexDirection as FoamFlexDirection
import com.foam.app.style.flex.JustifyContent
import com.foam.app.text.TextEngine
import dev.vfyjxf.taffy.geometry.TaffyRect

import dev.vfyjxf.taffy.geometry.TaffySize
import dev.vfyjxf.taffy.style.AlignContent
import dev.vfyjxf.taffy.style.AlignItems
import dev.vfyjxf.taffy.style.AvailableSpace
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.LengthPercentage
import dev.vfyjxf.taffy.style.LengthPercentageAuto
import dev.vfyjxf.taffy.style.TaffyDimension
import dev.vfyjxf.taffy.style.TaffyDisplay
import dev.vfyjxf.taffy.style.TaffyStyle
import dev.vfyjxf.taffy.tree.NodeId
import dev.vfyjxf.taffy.tree.TaffyTree
import org.w3c.dom.css.Rect


class TaffyLayoutEngine(
    private val textEngine: TextEngine
) : LayoutEngine {

    override fun layout(
        root: Node,
        width: Float,
        height: Float,
        scale: Float
    ) {

        val tree =
            TaffyTree()

        val nodeMap =
            mutableMapOf<Node, NodeId>()


        fun buildNode(
            node: Node,
            availableWidth: Float,
            isRoot: Boolean = false
        ): NodeId {

            val style =
                node.computedStyle


            /*
             * TEXT NODE
             */
            if (node is TextNode) {

                val measured =
                    textEngine.measure(
                        node,
                        availableWidth,
                        scale
                    )

                val textHeight =
                    measured.height / scale

                val taffyStyle =
                    TaffyStyle().apply {

                        size =
                            TaffySize(
                                TaffyDimension.length(
                                    availableWidth
                                ),
                                TaffyDimension.length(
                                    textHeight
                                )
                            )
                    }

                val taffyNode =
                    tree.newLeaf(
                        taffyStyle
                    )

                nodeMap[node] =
                    taffyNode

                return taffyNode
            }


            /*
             * NORMAL NODE
             */
            val nodeWidth =
                style.width

            val nodeHeight =
                style.height

            val horizontalPadding =
                style.paddingLeft +
                        style.paddingRight

            val contentWidth =
                nodeWidth
                    ?.let { it - horizontalPadding }
                    ?: (availableWidth - horizontalPadding)

            val childAvailableWidth =
                contentWidth
                    .coerceAtLeast(0f)

            val children =
                node.children.map { child ->

                    buildNode(
                        child,
                        childAvailableWidth
                    )
                }


            val taffyStyle =
                TaffyStyle().apply {

                    if (node.children.isNotEmpty()) {

                        display =
                            when (style.display) {
                                Display.FLEX ->
                                    TaffyDisplay.FLEX

                                Display.BLOCK ->
                                    TaffyDisplay.BLOCK

                                Display.NONE ->
                                    TaffyDisplay.NONE
                            }

                        flexDirection =
                            when (style.flexDirection) {
                                FoamFlexDirection.ROW -> FlexDirection.ROW

                                FoamFlexDirection.COLUMN -> FlexDirection.COLUMN
                            }

                        justifyContent =
                            when (style.justifyContent) {
                                JustifyContent.START -> AlignContent.START
                                JustifyContent.CENTER -> AlignContent.CENTER
                                JustifyContent.END -> AlignContent.END
                                JustifyContent.SPACE_BETWEEN -> AlignContent.SPACE_BETWEEN
                                JustifyContent.SPACE_AROUND -> AlignContent.SPACE_AROUND
                                JustifyContent.SPACE_EVENLY -> AlignContent.SPACE_EVENLY

                            }

                        alignItems =
                            when(style.alignItems) {
                                FoamAlignItems.CENTER -> AlignItems.CENTER
                                FoamAlignItems.END -> AlignItems.END
                                FoamAlignItems.START -> AlignItems.START
                                FoamAlignItems.STRETCH -> AlignItems.STRETCH
                            }

                        margin =
                            dev.vfyjxf.taffy.geometry.TaffyRect(
                                LengthPercentageAuto.length(
                                    style.marginLeft
                                ),
                                LengthPercentageAuto.length(
                                    style.marginRight
                                ),
                                LengthPercentageAuto.length(
                                    style.marginTop
                                ),
                                LengthPercentageAuto.length(
                                    style.marginBottom
                                )
                            )
                        padding =
                            TaffyRect(
                                LengthPercentage.length(
                                    style.paddingLeft
                                ),
                                LengthPercentage.length(
                                    style.paddingRight
                                ),
                                LengthPercentage.length(
                                    style.paddingTop
                                ),
                                LengthPercentage.length(
                                    style.paddingBottom
                                )
                            )
                    }

                    size =
                        if (isRoot) {

                            TaffySize(
                                TaffyDimension.length(
                                    width
                                ),
                                TaffyDimension.length(
                                    height
                                )
                            )

                        } else {

                            TaffySize(
                                nodeWidth?.let {
                                    TaffyDimension.length(it)
                                } ?: TaffyDimension.auto(),

                                nodeHeight?.let {
                                    TaffyDimension.length(it)
                                } ?: TaffyDimension.auto()
                            )
                        }

                    gap =
                        TaffySize(
                            LengthPercentage.length(
                                style.gap
                            ),
                            LengthPercentage.length(
                                0f
                            )
                        )
                }


            val taffyNode =

                if (children.isEmpty()) {

                    tree.newLeaf(
                        taffyStyle
                    )

                } else {

                    tree.newWithChildren(
                        taffyStyle,
                        children
                    )
                }


            nodeMap[node] =
                taffyNode

            return taffyNode
        }


        val rootNode =
            buildNode(
                root,
                width,
                true
            )


        tree.computeLayout(
            rootNode,
            TaffySize(
                AvailableSpace.definite(
                    width
                ),
                AvailableSpace.definite(
                    height
                )
            )
        )


        fun copyLayout(
            node: Node,
            parentX: Float,
            parentY: Float
        ) {

            val taffyNode =
                nodeMap[node]
                    ?: return

            val layout =
                tree.getLayout(
                    taffyNode
                )

            val absoluteX =
                parentX +
                        layout.location().x

            val absoluteY =
                parentY +
                        layout.location().y

            node.layout.x =
                absoluteX

            node.layout.y =
                absoluteY

            node.layout.width =
                layout.size().width

            node.layout.height =
                layout.size().height

            for (child in node.children) {

                copyLayout(
                    child,
                    absoluteX,
                    absoluteY
                )
            }
        }


        copyLayout(
            root,
            0f,
            0f
        )
    }
}