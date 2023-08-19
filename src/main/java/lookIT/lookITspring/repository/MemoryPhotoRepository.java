package lookIT.lookITspring.repository;

import java.util.List;
import lookIT.lookITspring.entity.MemoryPhoto;
import lookIT.lookITspring.entity.MemorySpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, Long> {

    List<MemoryPhoto> findAllByMemorySpot(MemorySpot memorySpot);

    MemoryPhoto findByMemorySpotSpotId(Long spotId);

    MemoryPhoto findByMemoryPhoto(String memoryPhoto);

}
