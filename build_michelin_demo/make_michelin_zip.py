import os
import zipfile
import shutil

root_dir = "build_michelin_demo"
out_zip_templates = "demo-templates/michelin_demo.zip"
out_zip_resources = "fastbite/adapter-in/web/src/main/resources/michelin_demo.zip"

os.makedirs("demo-templates", exist_ok=True)
os.makedirs("fastbite/adapter-in/web/src/main/resources", exist_ok=True)

with zipfile.ZipFile(out_zip_templates, "w", zipfile.ZIP_DEFLATED) as zipf:
    # 1. Add template.json
    zipf.write(os.path.join(root_dir, "template.json"), "template.json")
    # 2. Add data.json
    zipf.write(os.path.join(root_dir, "data.json"), "data.json")
    # 3. Add media files
    media_dir = os.path.join(root_dir, "media")
    if os.path.exists(media_dir):
        for root, _, files in os.walk(media_dir):
            for file in files:
                abs_path = os.path.join(root, file)
                rel_path = os.path.relpath(abs_path, root_dir).replace("\\", "/")
                zipf.write(abs_path, rel_path)

# Copy to resources
shutil.copyfile(out_zip_templates, out_zip_resources)

print(f"Successfully packaged {out_zip_templates} and {out_zip_resources}")
