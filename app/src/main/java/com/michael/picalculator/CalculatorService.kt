package com.michael.picalculator

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class CalculatorService : Service() {

    /** Current position after decimal point. */
    private var curPosition = AtomicLong(0L)
    /** Time interval in milliseconds between every two events. */
    private var timeIntervalMs = AtomicLong(100L)
    /** Algorithm logic providing calculating of PI */
    private var bpp = Bpp()
    /** Flag for pausing feature */
    @Volatile private var isPaused : Boolean = false
    /** Publisher for events. */
    private var publisher : PublishProcessor<String> = PublishProcessor.create()
    /** Interval disposable */
    private lateinit var timerDisposable : Disposable

    override fun onCreate() {
        super.onCreate()
        publisher.offer("3.")
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
        timerDisposable.dispose()
    }

    inner class Binder : android.os.Binder()  {

        fun setSpeed(ms: Long) {
            timeIntervalMs.set(ms)
        }

        fun start(): Flowable<String> {
            timerDisposable = Observable.interval(500L, timeIntervalMs.get(), TimeUnit.MILLISECONDS).map {
                if (curPosition.get() == 0L) {
                    publisher.offer("3.")
                } else {
                    publisher.offer(
                        String.format(Locale.getDefault(), "%09d", bpp.getDecimal(curPosition.get()))
                            .substring(0, 1)
                    )
                }
                curPosition.getAndIncrement()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe()

            return publisher
        }

        fun pause() {
            timerDisposable.dispose()
        }

        fun stop() {
            timerDisposable.dispose()
            curPosition = AtomicLong(0)
        }


    }

}
