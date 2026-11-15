#!/usr/bin/env python3
"""Генерирует плейсхолдер-текстуры, модели, blockstates, lang и loot-таблицы мода.
Запуск из корня проекта: python3 tools/generate_assets.py
Текстуры можно потом заменить своими (с теми же именами)."""
import json, os, random, struct, zlib

MOD = "horrorlab"
A = f"src/main/resources/assets/{MOD}"
D = f"src/main/resources/data/{MOD}"
random.seed(1337)

def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)

def jw(path, obj):
    write(path, json.dumps(obj, indent=2, ensure_ascii=False))

class Img:
    def __init__(s, w, h, bg=(0, 0, 0, 0)):
        s.w, s.h = w, h
        bg = bg if len(bg) == 4 else (*bg, 255)
        s.p = [[bg for _ in range(w)] for _ in range(h)]
    def px(s, x, y, c):
        if 0 <= x < s.w and 0 <= y < s.h:
            s.p[y][x] = c if len(c) == 4 else (*c, 255)
    def rect(s, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                s.px(x, y, c)
    def noise(s, amp):
        for y in range(s.h):
            for x in range(s.w):
                r, g, b, a = s.p[y][x]
                if a == 0: continue
                n = random.randint(-amp, amp)
                s.p[y][x] = (max(0, min(255, r + n)), max(0, min(255, g + n)), max(0, min(255, b + n)), a)
    def save(s, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        raw = b"".join(b"\x00" + bytes(c for px in row for c in px) for row in s.p)
        def ch(t, d):
            c = struct.pack(">I", len(d)) + t + d
            return c + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
        data = (b"\x89PNG\r\n\x1a\n" + ch(b"IHDR", struct.pack(">IIBBBBB", s.w, s.h, 8, 6, 0, 0, 0))
                + ch(b"IDAT", zlib.compress(raw, 9)) + ch(b"IEND", b""))
        with open(path, "wb") as f:
            f.write(data)

COLORS = {1: (60, 200, 90), 2: (60, 120, 230), 3: (220, 50, 50)}
T = f"{A}/textures"

# ---------- блоки ----------
im = Img(16, 16, (190, 195, 200))
for i in range(16):
    for j in range(16):
        if i % 8 == 0 or j % 8 == 0: im.px(i, j, (140, 146, 152))
for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13), (10, 2), (2, 10), (10, 10)]:
    im.px(x, y, (100, 104, 110))
im.noise(8); im.save(f"{T}/block/lab_wall.png")

im = Img(16, 16)
for y in range(16):
    for x in range(16):
        im.px(x, y, (62, 64, 68) if ((x // 8) + (y // 8)) % 2 == 0 else (46, 48, 52))
im.noise(6); im.save(f"{T}/block/lab_floor.png")

im = Img(16, 16, (205, 205, 195)); im.rect(2, 2, 13, 13, (255, 250, 215)); im.noise(4)
im.save(f"{T}/block/lab_lamp.png")

im = Img(16, 16, (90, 200, 70, 110))
for y in range(16):
    for x in range(16):
        im.px(x, y, (90 + random.randint(-20, 20), 200 + random.randint(-25, 25), 70, 95 + random.randint(0, 50)))
im.save(f"{T}/block/toxic_gas.png")

for lvl, col in COLORS.items():
    for half in ("bottom", "top"):
        im = Img(16, 16, (138, 144, 150))
        im.rect(0, 0, 15, 0, (90, 94, 100)); im.rect(0, 15, 15, 15, (90, 94, 100))
        im.rect(0, 0, 0, 15, (90, 94, 100)); im.rect(15, 0, 15, 15, (90, 94, 100))
        im.rect(2, 2, 13, 13, (122, 128, 134))
        if half == "bottom":
            im.rect(0, 6, 15, 8, col)
            im.rect(3, 11, 12, 12, (100, 106, 112))
        else:
            im.rect(4, 3, 11, 9, (35, 60, 70)); im.rect(5, 4, 7, 5, (70, 110, 125))
            im.rect(12, 11, 14, 13, (25, 25, 25)); im.rect(13, 12, 13, 12, col)
            im.rect(0, 14, 15, 15, col)
        im.noise(5); im.save(f"{T}/block/lab_door_{lvl}_{half}.png")
    # иконка двери для предмета
    im = Img(16, 16)
    im.rect(4, 1, 11, 14, (138, 144, 150)); im.rect(4, 1, 11, 1, (90, 94, 100)); im.rect(4, 14, 11, 14, (90, 94, 100))
    im.rect(4, 1, 4, 14, (90, 94, 100)); im.rect(11, 1, 11, 14, (90, 94, 100))
    im.rect(5, 6, 10, 8, col); im.rect(6, 3, 9, 4, (35, 60, 70)); im.px(9, 11, col)
    im.save(f"{T}/item/lab_door_{lvl}.png")

# ---------- предметы ----------
for lvl, col in COLORS.items():
    im = Img(16, 16)
    im.rect(2, 4, 13, 12, (235, 235, 235)); im.rect(2, 4, 13, 4, (170, 170, 170)); im.rect(2, 12, 13, 12, (170, 170, 170))
    im.rect(2, 4, 2, 12, (170, 170, 170)); im.rect(13, 4, 13, 12, (170, 170, 170))
    im.rect(3, 6, 12, 7, col)
    im.rect(4, 9, 6, 10, (230, 190, 60)); im.rect(9, 9, 12, 9, (90, 90, 90)); im.rect(9, 11, 11, 11, (90, 90, 90))
    im.save(f"{T}/item/keycard_{lvl}.png")

im = Img(16, 16)
im.rect(2, 4, 13, 6, (70, 72, 78)); im.rect(2, 4, 13, 4, (95, 98, 105))
im.rect(11, 6, 13, 6, (50, 52, 56)); im.rect(1, 5, 1, 6, (50, 52, 56))
im.rect(4, 7, 7, 12, (45, 40, 36)); im.rect(5, 7, 5, 12, (60, 52, 46))
im.rect(8, 7, 9, 8, (60, 62, 66)); im.px(8, 9, (60, 62, 66))
im.save(f"{T}/item/pistol.png")

im = Img(16, 16)
for x in (3, 7, 11):
    im.rect(x, 4, x + 2, 12, (210, 170, 60)); im.rect(x, 4, x + 2, 6, (180, 100, 40))
im.save(f"{T}/item/ammo.png")

im = Img(16, 16)
im.rect(4, 3, 11, 11, (55, 60, 55)); im.rect(5, 2, 10, 2, (55, 60, 55)); im.rect(6, 12, 9, 14, (90, 95, 90))
im.rect(5, 6, 6, 8, (120, 230, 120)); im.rect(9, 6, 10, 8, (120, 230, 120))
im.px(7, 13, (40, 40, 40)); im.px(8, 13, (40, 40, 40))
im.save(f"{T}/item/gas_mask.png")

im = Img(16, 16)
im.rect(2, 4, 13, 13, (240, 240, 240)); im.rect(2, 4, 13, 4, (200, 200, 200)); im.rect(6, 2, 9, 3, (150, 150, 150))
im.rect(7, 6, 8, 11, (210, 30, 30)); im.rect(5, 8, 10, 9, (210, 30, 30))
im.save(f"{T}/item/medkit.png")

# ---------- сущность (64x64, раскладка зомби) ----------
im = Img(64, 64, (84, 104, 82)); im.noise(14)
for _ in range(60):
    x, y = random.randint(0, 63), random.randint(0, 63)
    im.rect(x, y, x + random.randint(0, 2), y + random.randint(0, 2), (60, 40, 40))
im.rect(9, 11, 10, 12, (255, 30, 30)); im.rect(13, 11, 14, 12, (255, 30, 30))
im.rect(10, 14, 13, 14, (30, 15, 15))
im.save(f"{T}/entity/mutant.png")

# ---------- модели ----------
def cube(name, tex=None):
    jw(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{tex or name}"}})
    jw(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    jw(f"{A}/models/item/{name}.json", {"parent": f"{MOD}:block/{name}"})
for n in ("lab_wall", "lab_floor", "lab_lamp", "toxic_gas"):
    cube(n)

def gen_item(name):
    jw(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}})
for n in ("keycard_1", "keycard_2", "keycard_3", "pistol", "ammo", "gas_mask", "medkit"):
    gen_item(n)
jw(f"{A}/models/item/mutant_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

for lvl in COLORS:
    name = f"lab_door_{lvl}"
    gen_item(name)
    variants = {}
    for half in ("bottom", "top"):
        for hinge in ("left", "right"):
            for open_ in (False, True):
                model = f"{half}_{hinge}" + ("_open" if open_ else "")
                jw(f"{A}/models/block/{name}_{model}.json", {
                    "parent": f"minecraft:block/door_{model}",
                    "textures": {"bottom": f"{MOD}:block/{name}_bottom", "top": f"{MOD}:block/{name}_top"}})
                for facing, base in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
                    if not open_: y = base
                    elif hinge == "left": y = (base + 90) % 360
                    else: y = (base + 270) % 360
                    v = {"model": f"{MOD}:block/{name}_{model}"}
                    if y: v["y"] = y
                    variants[f"facing={facing},half={'lower' if half == 'bottom' else 'upper'},hinge={hinge},open={str(open_).lower()}"] = v
    jw(f"{A}/blockstates/{name}.json", {"variants": variants})

# ---------- loot / tags ----------
for n in ("lab_wall", "lab_floor", "lab_lamp"):
    jw(f"{D}/loot_table/blocks/{n}.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "conditions": [{"condition": "minecraft:survives_explosion"}],
                   "entries": [{"type": "minecraft:item", "name": f"{MOD}:{n}"}]}]})
jw("src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json",
   {"replace": False, "values": [f"{MOD}:lab_wall", f"{MOD}:lab_floor", f"{MOD}:lab_lamp"]})

# ---------- lang ----------
en = {
 "itemGroup.horrorlab.main": "Horror Lab",
 "item.horrorlab.keycard_1": "Keycard (Level 1)", "item.horrorlab.keycard_2": "Keycard (Level 2)",
 "item.horrorlab.keycard_3": "Keycard (Level 3)", "item.horrorlab.gas_mask": "Gas Mask",
 "item.horrorlab.pistol": "Pistol", "item.horrorlab.ammo": "Pistol Ammo", "item.horrorlab.medkit": "Medkit",
 "item.horrorlab.mutant_spawn_egg": "Mutant Spawn Egg", "entity.horrorlab.mutant": "Lab Mutant",
 "block.horrorlab.lab_wall": "Lab Wall", "block.horrorlab.lab_floor": "Lab Floor", "block.horrorlab.lab_lamp": "Lab Lamp",
 "block.horrorlab.toxic_gas": "Toxic Gas",
 "block.horrorlab.lab_door_1": "Lab Door (Level 1)", "block.horrorlab.lab_door_2": "Lab Door (Level 2)",
 "block.horrorlab.lab_door_3": "Lab Door (Level 3)",
 "tooltip.horrorlab.keycard": "Access level %s", "message.horrorlab.need_key": "Access denied: keycard level %s required",
 "command.horrorlab.built": "Laboratory built at %s %s %s (entrance is in front of you, south)",
}
ru = {
 "itemGroup.horrorlab.main": "Хоррор-лаборатория",
 "item.horrorlab.keycard_1": "Карточка доступа (ур. 1)", "item.horrorlab.keycard_2": "Карточка доступа (ур. 2)",
 "item.horrorlab.keycard_3": "Карточка доступа (ур. 3)", "item.horrorlab.gas_mask": "Противогаз",
 "item.horrorlab.pistol": "Пистолет", "item.horrorlab.ammo": "Патроны", "item.horrorlab.medkit": "Аптечка",
 "item.horrorlab.mutant_spawn_egg": "Яйцо призыва мутанта", "entity.horrorlab.mutant": "Мутант лаборатории",
 "block.horrorlab.lab_wall": "Стена лаборатории", "block.horrorlab.lab_floor": "Пол лаборатории", "block.horrorlab.lab_lamp": "Лампа лаборатории",
 "block.horrorlab.toxic_gas": "Ядовитый газ",
 "block.horrorlab.lab_door_1": "Дверь лаборатории (ур. 1)", "block.horrorlab.lab_door_2": "Дверь лаборатории (ур. 2)",
 "block.horrorlab.lab_door_3": "Дверь лаборатории (ур. 3)",
 "tooltip.horrorlab.keycard": "Уровень доступа %s", "message.horrorlab.need_key": "Доступ запрещён: нужна карточка уровня %s",
 "command.horrorlab.built": "Лаборатория построена в %s %s %s (вход прямо перед вами, на юге)",
}
jw(f"{A}/lang/en_us.json", en)
jw(f"{A}/lang/ru_ru.json", ru)
print("assets generated")
