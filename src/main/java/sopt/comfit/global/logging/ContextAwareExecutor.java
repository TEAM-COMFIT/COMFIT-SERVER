package sopt.comfit.global.logging;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class ContextAwareExecutor {

    private final ContextSnapshotFactory contextSnapshotFactory;


    public <T> Future<T> submit(ExecutorService executor, Callable<T> task) {
        // 현재 스레드의 모든 ThreadLocal 캡처
        ContextSnapshot snapshot = contextSnapshotFactory.captureAll();

        return executor.submit(() -> {
            // VT에서 컨텍스트 복원 후 실행
            try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
                return task.call();
            }
        });
    }

    public ExecutorService newVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
