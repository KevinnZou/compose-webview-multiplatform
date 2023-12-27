package com.kevinnzou.sample.eventbus

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlin.reflect.KClass

/**
 * Created By Kevin Zou On 2023/12/15
 */

typealias Observer = (Any) -> Unit

object EventBus {
    private val observers = atomic(mutableMapOf<KClass<out Any>, List<Observer>>())

    fun <T : Any> observe(
        clazz: KClass<T>,
        obs: (T) -> Unit,
    ) {
        if (!observers.value.containsKey(clazz)) {
            observers.getAndUpdate { cur ->
                cur.toMutableMap().also { upd ->
                    upd[clazz] = listOf(obs as Observer)
                }
            }
        } else {
            observers.getAndUpdate { cur ->
                cur.toMutableMap().also { upd ->
                    upd[clazz] = upd[clazz]!! + listOf(obs as Observer)
                }
            }
        }
    }

    inline fun <reified T : Any> observe(noinline obs: (T) -> Unit) {
        observe(T::class, obs)
    }

    fun <T : Any> removeObserver(
        clazz: KClass<T>,
        obs: (T) -> Unit,
    ) {
        observers.getAndUpdate { cur ->
            cur.toMutableMap().also { upd ->
                upd.remove(clazz)
            }
        }
    }

    fun <T : Any> post(
        clazz: KClass<T>,
        event: T,
    ) {
        observers.value[clazz]?.forEach { it.invoke(event) }
    }

    inline fun <reified T : Any> post(event: T) {
        post(T::class, event)
    }
}
