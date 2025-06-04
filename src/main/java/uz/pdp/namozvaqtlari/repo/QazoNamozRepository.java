package uz.pdp.namozvaqtlari.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.namozvaqtlari.entity.QazoNamoz;
import uz.pdp.namozvaqtlari.entity.Users;

import java.util.List;

public interface QazoNamozRepository extends JpaRepository<QazoNamoz, Long> {
  List<QazoNamoz> findByUsers(Users users);
}