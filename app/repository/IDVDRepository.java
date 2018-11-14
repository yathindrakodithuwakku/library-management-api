package repository;

import com.google.inject.ImplementedBy;
import models.DVD;
import org.mongodb.morphia.Key;

import java.util.List;
import repository.implementation.DVDRepositoryImpl;

@ImplementedBy(DVDRepositoryImpl.class)
public interface IDVDRepository {
    Key<DVD> save(DVD item);

    List<DVD> findAll();
}
