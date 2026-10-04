package es.brasatech.fastbite.application.tenant;
import es.brasatech.fastbite.domain.tenant.SetupProgress;
import java.util.function.UnaryOperator;
public interface OwnerWorkspacePort {
    SetupProgress read(String tenant);
    void update(String tenant, UnaryOperator<SetupProgress> change);
}
