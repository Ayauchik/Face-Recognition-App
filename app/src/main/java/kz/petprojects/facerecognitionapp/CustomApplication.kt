package kz.petprojects.facerecognitionapp

import android.app.Application
import kz.petprojects.facerecognitionapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

class CustomApplication : Application() {
    private val modulesToUse = listOf(
        //repositoryModule,
        viewModelModule,
        //useCaseModule,
        //mapperModule,
        //roomDatabaseModule
    )

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@CustomApplication)
            parametersOf()
            modules(modulesToUse)
        }
    }
}