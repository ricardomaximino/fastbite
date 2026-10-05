from PIL import Image, ImageDraw, ImageFont
import os

images = [
    ("tartar-atun.webp", "Tartar de Atun Rojo", "Balfego & Caviar Oscietra"),
    ("carpaccio-wagyu.webp", "Carpaccio Wagyu A5", "Trufa Negra Melanosporum"),
    ("vieira-braseada.webp", "Vieira Braseada", "Crema Coliflor Trufada"),
    ("lubina-salvaje.webp", "Lubina Salvaje", "Emulsion de Plancton Marino"),
    ("bogavante-azul.webp", "Bogavante Azul", "Bisque de sus Corales"),
    ("solomillo-rubia-gallega.webp", "Solomillo Rubia Gallega", "Pure Robuchon 60 Dias"),
    ("pichon-bresse.webp", "Pichon de Bresse", "Dos Cocciones & Foie Gras"),
    ("menu-degustacion.webp", "Menu Degustacion", "Signature 8 Pases Michelin"),
    ("esfera-chocolate.webp", "Esfera Dorada", "Chocolate Valrhona Grand Cru"),
    ("texturas-citricas.webp", "Texturas de Citricos", "Sorbete Albahaca Fresca"),
    ("coctel-ahumado.webp", "Old Fashioned Ahumado", "Trufa Negra & Naranja"),
    ("champagne-domperignon.webp", "Dom Perignon Vintage", "Brut Reserve en Copa")
]

os.makedirs("build_michelin_demo/media/michelin", exist_ok=True)

for filename, title, subtitle in images:
    img = Image.new("RGB", (600, 600), color=(18, 20, 24))
    draw = ImageDraw.Draw(img)
    
    # Outer luxury gold border
    draw.rectangle([(20, 20), (580, 580)], outline=(212, 175, 55), width=3)
    draw.rectangle([(30, 30), (570, 570)], outline=(140, 115, 35), width=1)
    
    # Michelin stars symbol
    draw.text((300, 180), "★ ★ ★", fill=(212, 175, 55), anchor="mm")
    
    # Dish name and subtitle
    draw.text((300, 270), title, fill=(245, 245, 245), anchor="mm")
    draw.text((300, 320), subtitle, fill=(180, 180, 180), anchor="mm")
    draw.text((300, 420), "HAUTE CUISINE RESTAURANT", fill=(212, 175, 55), anchor="mm")
    
    out_path = os.path.join("build_michelin_demo/media/michelin", filename)
    img.save(out_path, "WEBP", quality=90)
    print(f"Generated {out_path}")

print("All temporary WebP assets generated!")
