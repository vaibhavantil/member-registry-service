package com.hedvig.memberservice.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.util.EnumMapChecker;
import com.hedvig.memberservice.web.dto.Market;
import com.hedvig.memberservice.web.dto.MembersSortColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Table(
    name = "member_entity",
    indexes = {@Index(columnList = "ssn", name = "ix_member_entity_ssn")})
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

  public String acceptLanguage;

  @Nullable
  @Enumerated(EnumType.STRING)
  public Market market;

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

  public Long getId() {
    return id;
  }

  public MemberStatus getStatus() {
    return status;
  }

  public String getSsn() {
    return ssn;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getEmail() {
    return email;
  }

  public String getStreet() {
    return street;
  }

  public String getCity() {
    return city;
  }

  public String getZipCode() {
    return zipCode;
  }

  public Integer getFloor() {
    return floor;
  }

  public String getApartment() {
    return apartment;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public Instant getSignedOn() {
    return signedOn;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public FraudulentStatus getFraudulentStatus() {
    return fraudulentStatus;
  }

  public String getFraudulentDescription() {
    return fraudulentDescription;
  }

  public String getAcceptLanguage() {
    return acceptLanguage;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public void setApartment(String apartment) {
    this.apartment = apartment;
  }

  public void setStatus(MemberStatus status) {
    this.status = status;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setCashbackId(String cashbackId) {
    this.cashbackId = cashbackId;
  }

  public void setAcceptLanguage(String acceptLanguage) {
    this.acceptLanguage = acceptLanguage;
  }

  public void setMarket(Market market) { this.market = market; }

  public void setFloor(Integer floor) {
    this.floor = floor;
  }

  public void setSignedOn(Instant signedOn) {
    this.signedOn = signedOn;
  }

  public void setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
  }

  public void setFraudulentStatus(FraudulentStatus fraudulentStatus) {
    this.fraudulentStatus = fraudulentStatus;
  }

  public void setFraudulentDescription(String fraudulentDescription) {
    this.fraudulentDescription = fraudulentDescription;
  }

  public String getCashbackId() {
    return cashbackId;
  }
}
