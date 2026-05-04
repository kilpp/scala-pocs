package com.example

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing

import java.util.{List as JList, Optional}

// `@EnableJdbcAuditing` activates the @CreatedDate/@LastModifiedDate/
// @CreatedBy/@LastModifiedBy populators on the `Person` entity.
//
// `auditorAware` supplies the value used for @CreatedBy/@LastModifiedBy.
// In a real app this would pull the current principal from SecurityContext.
//
// `jdbcCustomConversions` registers the Email ↔ VARCHAR converters so
// Spring Data can read/write the `Person.email` column.
@Configuration
@EnableJdbcAuditing
class JdbcConfig:

  @Bean
  def auditorAware: AuditorAware[String] =
    () => Optional.of("system")

  @Bean
  def jdbcCustomConversions: JdbcCustomConversions =
    JdbcCustomConversions(JList.of(Email.ToString(), Email.FromString()))
