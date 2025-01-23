package kz.petprojects.facerecognitionapp.di

import kz.petprojects.facerecognitionapp.FaceContourViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { FaceContourViewModel() }
}