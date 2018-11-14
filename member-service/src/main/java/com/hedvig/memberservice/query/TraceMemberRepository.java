package com.hedvig.memberservice.query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TraceMemberRepository extends CrudRepository<TraceMemberEntity, String>  {
  @Query("from TraceMemberEntity tpe where tpe.memberId = :memberId")
  List <TraceMemberEntity> findByMemberId(@Param("memberId") Long productId);
}
