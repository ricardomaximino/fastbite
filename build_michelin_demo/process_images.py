from PIL import Image
import os
import shutil

source_dir = r"C:\Users\ricar\.gemini\antigravity\brain\896823f4-46d6-4d20-a419-cbee23db4141"
target_dir = r"D:\git\fastbite\build_michelin_demo\media\michelin"

mappings = [
    ("tartar_atun_1791225132628.jpg", "tartar-atun.webp"),
    ("carpaccio_wagyu_1791225148878.jpg", "carpaccio-wagyu.webp"),
    ("vieira_braseada_1791225164844.jpg", "vieira-braseada.webp"),
    ("lubina_salvaje_1791225186154.jpg", "lubina-salvaje.webp"),
    ("bogavante_azul_1791225206518.jpg", "bogavante-azul.webp"),
    ("solomillo_rubia_1791225224597.jpg", "solomillo-rubia-gallega.webp"),
    ("pichon_bresse_1791225247313.jpg", "pichon-bresse.webp"),
    ("menu_degustacion_1791225268550.jpg", "menu-degustacion.webp"),
    ("esfera_chocolate_1791225288912.jpg", "esfera-chocolate.webp"),
    ("texturas_citricas_1791225309283.jpg", "texturas-citricas.webp"),
    ("coctel_ahumado_1791225329034.jpg", "coctel-ahumado.webp"),
    ("champagne_glass_1791225351099.jpg", "champagne-domperignon.webp"),
]

os.makedirs(target_dir, exist_ok=True)

for src_name, dst_name in mappings:
    src_path = os.path.join(source_dir, src_name)
    dst_path = os.path.join(target_dir, dst_name)
    
    with Image.open(src_path) as img:
        img = img.convert("RGB")
        # Center crop to square
        w, h = img.size
        min_dim = min(w, h)
        left = (w - min_dim) // 2
        top = (h - min_dim) // 2
        img_cropped = img.crop((left, top, left + min_dim, top + min_dim))
        
        # Resize to 600x600
        img_resized = img_cropped.resize((600, 600), Image.Resampling.LANCZOS)
        
        # Save as WebP
        img_resized.save(dst_path, "WEBP", quality=85)
        print(f"Processed: {src_name} -> {dst_name} ({img_resized.size})")

print("All 12 Michelin images successfully processed to 600x600 WebP!")
