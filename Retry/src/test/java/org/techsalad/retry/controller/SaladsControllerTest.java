package org.techsalad.retry.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.techsalad.retry.service.SaladsService;

import io.github.resilience4j.retry.autoconfigure.RetryAutoConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {RetryAutoConfiguration.class,
        AnnotationAwareAspectJAutoProxyCreator.class, SaladsController.class},
    properties = {"resilience4j.retry.instances.getSalads.maxRetryAttempts=5",
        "resilience4j.retry.instances.getSalads.waitDuration=1ms"})
class SaladsControllerTest {

  private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException("Zle, nedobre!");

  @MockBean
  private SaladsService saladsService;

  @Autowired
  private SaladsController saladsController;

  @Test
  void testExecuteWithUnsuccessful5thAttempts() {
    // given
    when(this.saladsService.getAllSalads()).thenThrow(RUNTIME_EXCEPTION);

    // when
    final Executable getSalads = () -> saladsController.getSalads();

    // then
    assertThrows(RuntimeException.class, getSalads, "Hmm... tu to malo spadnut!??");
    verify(this.saladsService, times(5)).getAllSalads();
  }

  @Test
  void testExecuteWithSuccessful3rdAttempt() {
    // given
    final String expectedSalad = "Najsamlepší šalátik";
    when(this.saladsService.getAllSalads()).thenThrow(RUNTIME_EXCEPTION)
        .thenThrow(RUNTIME_EXCEPTION).thenReturn(Collections.singletonList(expectedSalad));

    // when
    final List<String> salads = saladsController.getSalads();

    // then
    verify(this.saladsService, times(3)).getAllSalads();
    assertEquals(expectedSalad, salads.get(0));
  }

}
