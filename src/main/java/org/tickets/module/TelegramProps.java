package org.tickets.module;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD,ElementType.CONSTRUCTOR,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface TelegramProps {
}
