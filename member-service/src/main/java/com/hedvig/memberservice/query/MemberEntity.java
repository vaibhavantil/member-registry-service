package com.hedvig.memberservice.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.util.EnumMapChecker;
import com.hedvig.memberservice.web.dto.MembersSortColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Table(
    name = "member_entity",
    indexes = {@Index(columnList = "ssn", name = "ix_member_entity_ssn")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {

  @Id public Long id;

  String zipCode;
  public String apartment;

  @Enumerated(EnumType.STRING)
  public MemberStatus status;
  public String ssn;
  public String firstName;
  public String lastName;
  public LocalDate birthDate;
  public String street;
  public String city;

  public String phoneNumber;

  public String email;

  public String cashbackId;

  public Integer floor;

  public Instant signedOn;

  public Instant createdOn;

  @Enumerated(EnumType.STRING)
  public FraudulentStatus fraudulentStatus = FraudulentStatus.NOT_FRAUD;
  public String fraudulentDescription;

  @Formula("first_name || ' ' || last_name")
  public String fullName;

  public static final EnumMap<MembersSortColumn, String> SORT_COLUMN_MAPPING = new EnumMap<MembersSortColumn, String>(MembersSortColumn.class) {{
    put(MembersSortColumn.CREATED, "createdOn");
    put(MembersSortColumn.NAME, "fullName");
    put(MembersSortColumn.SIGN_UP, "signedOn");

    EnumMapChecker.ensureMapContainsAllEnumVals(this, MembersSortColumn.class);
  }};
}
