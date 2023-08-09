package lookIT.lookITspring.repository;


import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.MemorySpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
public interface MemorySpotRepository extends JpaRepository<MemorySpot, Long> {

    List<MemorySpot> findAllByMemory(Memory memory);

    List<MemorySpot> findBySpotLatitudeAndSpotLongitude(Double spotLatitude, Double spotLongitude);

    List<MemorySpot> findByMemoryMemoryId(Long memoryId);
}
