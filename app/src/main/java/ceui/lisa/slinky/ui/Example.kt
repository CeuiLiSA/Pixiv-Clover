package ceui.lisa.slinky.ui

import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.databinding.ItemExampleBinding

class ExampleHolder : SlinkyItem()


@ItemHolder(ExampleHolder::class)
class ExampleViewHolder(aa: ItemExampleBinding) :
    SlinkyViewHolder<ItemExampleBinding, ExampleHolder>(aa)