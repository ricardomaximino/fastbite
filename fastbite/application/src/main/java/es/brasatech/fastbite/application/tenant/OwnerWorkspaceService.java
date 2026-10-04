package es.brasatech.fastbite.application.tenant;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.settings.RestaurantSettingsService;
import es.brasatech.fastbite.domain.settings.RestaurantSettings;
import es.brasatech.fastbite.domain.tenant.*;
import org.springframework.stereotype.Service;
import java.util.function.Supplier;
@Service
public class OwnerWorkspaceService {
    private final TenantLocationService locations;
    private final OwnerWorkspacePort progress;
    private final ProductService products;
    private final RestaurantSettingsService settings;
    public OwnerWorkspaceService(TenantLocationService locations, OwnerWorkspacePort progress, ProductService products, RestaurantSettingsService settings) {
        this.locations=locations; this.progress=progress; this.products=products; this.settings=settings;
    }
    public void requireOwner(String owner, String tenant) {
        if (owner == null || !locations.isOwnerOf(owner,tenant)) throw new IllegalArgumentException("You do not own this location.");
    }
    public Summary summary(String owner,String tenant) {
        requireOwner(owner,tenant);
        return inTenant(tenant,()->new Summary(progress.read(tenant), products.findAll().size(), settings.get()));
    }
    public void action(String owner,String tenant,String action) {
        requireOwner(owner,tenant);
        progress.update(tenant,p->switch(action) {
            case "start" -> new SetupProgress(true,p.serviceReviewed(),p.previewReviewed(),false);
            case "preview" -> new SetupProgress(true,p.serviceReviewed(),true,p.dismissed());
            case "dismiss" -> new SetupProgress(p.started(),p.serviceReviewed(),p.previewReviewed(),true);
            case "resume" -> new SetupProgress(p.started(),p.serviceReviewed(),p.previewReviewed(),false);
            default -> throw new IllegalArgumentException("Unknown setup action.");
        });
    }
    public void saveService(String owner,String tenant,boolean dineIn,boolean takeaway,int yellow,int red) {
        requireOwner(owner,tenant);
        if (!dineIn && !takeaway) throw new IllegalArgumentException("Choose at least one way to serve your customers.");
        if (yellow<0 || red<0 || (yellow>0 && red>0 && red<yellow)) throw new IllegalArgumentException("Kitchen warning times must be positive and in increasing order, or zero to disable.");
        inTenant(tenant,()->settings.save(new RestaurantSettings(dineIn,takeaway,yellow,red)));
        progress.update(tenant,p->new SetupProgress(true,true,p.previewReviewed(),p.dismissed()));
    }
    private <T> T inTenant(String tenant,Supplier<T> work) {
        String before=TenantContext.getCurrentTenant();
        try { TenantContext.setCurrentTenant(tenant); return work.get(); }
        finally { if(before==null) TenantContext.clear(); else TenantContext.setCurrentTenant(before); }
    }
    public record Summary(SetupProgress progress,int products,RestaurantSettings settings) {
        public int completed() { return (products>0?1:0)+(progress.serviceReviewed()?1:0)+(progress.previewReviewed()?1:0); }
        public boolean ready() { return completed()==3; }
        public boolean needsStart() { return products==0 && !progress.started(); }
    }
}
