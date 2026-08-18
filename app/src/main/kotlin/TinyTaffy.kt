package com.foam.app

import dev.vfyjxf.taffy.tree.TaffyTree
import dev.vfyjxf.taffy.style.TaffyStyle
import dev.vfyjxf.taffy.style.TaffyDimension

import dev.vfyjxf.taffy.geometry.TaffySize
import dev.vfyjxf.taffy.style.AvailableSpace
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.LengthPercentage
import dev.vfyjxf.taffy.style.TaffyDisplay

fun main() {
    val tree = TaffyTree()
    val childStyle = TaffyStyle().apply {
        size = TaffySize(
            TaffyDimension.length(100f),
            TaffyDimension.length(100f)
        )
    }

    val child1 = tree.newLeaf(childStyle)
    val child2 = tree.newLeaf(childStyle)

    val rootStyle = TaffyStyle().apply {
        display = TaffyDisplay.FLEX
        flexDirection = FlexDirection.ROW

        gap = TaffySize(
            LengthPercentage.length(20f),
            LengthPercentage.length(0f)
        )
    }
    val root = tree.newWithChildren(
        rootStyle,
        child1,
        child2
    )

    tree.computeLayout(
        root,
        TaffySize(
            AvailableSpace.definite(800f),
            AvailableSpace.definite(600f)
        )
    )

    val rootLayout = tree.getLayout(root)
    val child1Layout = tree.getLayout(child1)
    val child2Layout = tree.getLayout(child2)

    println("Root layout: $rootLayout\n")
    println("Child 1 layout: $child1Layout\n")
    println("Child 2 layout: $child2Layout\n")
}
