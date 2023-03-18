package com.kiylx.crashertools.utils

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


////需要指定初始值的情况
//        //自动推断出泛型
//        var act by Weak{
//            context
//        }
//        //也可以指定泛型，一种是给属性指定类型，必须为可null的
//        var act: Activity? by Weak {
//            context
//        }
//        //第二种是为Weak指定泛型，不可null的
//        var act by Weak<Activity> {
//            context
//        }
//
////不指定初始值的情况，此时必须指定泛型
//        var act:Activity? by Weak()
//        或者
//        var act by Weak<Activity>()
class Weak<T : Any>(initializer: () -> T?) : ReadWriteProperty<Any?, T?> {
    var weakReference = WeakReference<T?>(initializer())

    //次级构造函数，最终是调用主构造函数
    constructor() : this(initializer = fun(): T? {
        return null
    })

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        //Log.d("Weak Delegate", "-----------getValue")
        return weakReference.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        //Log.d("Weak Delegate", "-----------setValue")
        weakReference = WeakReference(value)
    }


}