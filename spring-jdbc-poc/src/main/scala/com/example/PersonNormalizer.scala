package com.example

import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component

// Lifecycle callback fired before an aggregate is converted to a SQL row.
// Useful for normalization, derived fields, validation. Returns the (possibly
// modified) entity; the returned instance is what's actually persisted.
@Component
class PersonNormalizer extends BeforeConvertCallback[Person]:
  override def onBeforeConvert(person: Person): Person =
    person.copy(name = person.name.trim)
