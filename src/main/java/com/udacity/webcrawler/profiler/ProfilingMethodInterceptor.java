package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object target;
  private final ProfilingState state;
  private final ZonedDateTime timeStart;

  ProfilingMethodInterceptor(Clock clock,Object target, ProfilingState state, ZonedDateTime timeStart) {
    this.clock = Objects.requireNonNull(clock);
    this.target = Objects.requireNonNull(target);
    this.state = Objects.requireNonNull(state);
    this.timeStart = Objects.requireNonNull(timeStart);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object ans = null;
    Instant start = null;
    boolean profiled = method.getAnnotation(Profiled.class) != null;

    if (profiled) {
      start = clock.instant();
    }

    try {
      ans = method.invoke(this.target, args);
    } catch (InvocationTargetException invocationTargetException) {
      throw invocationTargetException.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      if (profiled) {
        Duration duration = Duration.between(start, clock.instant());
        state.record(this.target.getClass(), method, duration);
      }
    }
    return ans;
  }

  @Override
  public boolean equals(Object object){

    if(object == this) return true;
    if(!(object instanceof ProfilingMethodInterceptor)) return false;
    ProfilingMethodInterceptor methodInterceptor = (ProfilingMethodInterceptor) object;
    return Objects.equals(this.clock, methodInterceptor.clock) && Objects.equals(this.target,methodInterceptor.target) && Objects.equals(this.state,methodInterceptor.state) && Objects.equals(this.timeStart,methodInterceptor.timeStart);

  }
}