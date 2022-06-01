package org.techsalad.timeout.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.techsalad.timeout.service.SaladsService;

import io.github.resilience4j.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {TimeLimiterAutoConfiguration.class, AnnotationAwareAspectJAutoProxyCreator.class,
        SaladsController.class, SaladsService.class},
    properties = {"resilience4j.timelimiter.instances.getSalads.timeoutDuration=500ms"})
class SaladsControllerTest {

  @Autowired
  private SaladsService saladsService;

  @Autowired
  private SaladsController saladsController;

  @Test
  void testExecuteWithTimeout() {
    // given
    ReflectionTestUtils.setField(saladsService, "waitDuration", Duration.ofMillis(600));

    // when
    final Executable getSalads = () -> saladsController.getSalads();

    // then
    final RuntimeException runtimeException = assertThrows(RuntimeException.class, getSalads);
    assertEquals(ExecutionException.class, runtimeException.getCause().getClass());
    assertEquals(TimeoutException.class, runtimeException.getCause().getCause().getClass());
  }

  @Test
  void testExecuteSuccessfully() {
    // given
    ReflectionTestUtils.setField(saladsService, "waitDuration", Duration.ofMillis(100));

    // when
    final List<String> salads = saladsController.getSalads();

    // then
    assertEquals("The best salad ever!", salads.get(0));
  }

}
