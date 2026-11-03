package com.example.ui.persona;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;

import java.util.List;

public class PersonaViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<PersonaEntity>> allPersonas;
    private final LiveData<PersonaEntity> activePersona;

    public PersonaViewModel(@NonNull Application application) {
        super(application);
        this.repository = AppRepository.getInstance(application);
        this.allPersonas = repository.getAllPersonasLive();
        this.activePersona = repository.getActivePersonaLive();
    }

    public LiveData<List<PersonaEntity>> getAllPersonas() {
        return allPersonas;
    }

    public LiveData<PersonaEntity> getActivePersona() {
        return activePersona;
    }

    public void setActivePersona(PersonaEntity persona) {
        repository.setActivePersona(persona);
    }

    public void savePersona(PersonaEntity persona, Runnable onComplete) {
        repository.savePersona(persona, onComplete);
    }

    public void deletePersona(PersonaEntity persona, Runnable onComplete) {
        repository.deletePersona(persona, onComplete);
    }
}
