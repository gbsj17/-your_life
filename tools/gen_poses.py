#!/usr/bin/env python3
"""Gera android/.../data/Poses.kt (poses A e B de cada exercício) e, opcionalmente,
imagens de prévia para conferir as animações.

Uso:
    python3 tools/gen_poses.py kotlin  <caminho/Poses.kt>
    python3 tools/gen_poses.py preview <pasta_de_saida>

Coordenadas: x para a direita, y para cima, chão em y = 0, quadro de 100 x 75.
Cada membro (braço/perna) é definido pelo ponto onde a mão/o tornozelo fica
(cinemática inversa de 2 ossos), então os pés e as mãos ficam firmes no chão.
O app interpola a pose A -> B em loop para animar o exercício.
"""
import math
import sys

# comprimentos dos segmentos (unidades do quadro)
UA, FA, TH, SH, TORSO, NECK, HR, FOOT = 12.0, 11.0, 15.0, 15.0, 22.0, 4.0, 5.5, 5.5
ANK = 1.2  # altura do tornozelo com o pé apoiado no chão


def d(a):
    r = math.radians(a)
    return math.cos(r), math.sin(r)


def ik(root, target, l1, l2, bend):
    dx, dy = target[0] - root[0], target[1] - root[1]
    dist = math.hypot(dx, dy) or 1e-6
    dd = min(max(dist, abs(l1 - l2) + 0.05), l1 + l2 - 0.05)
    ux, uy = dx / dist, dy / dist
    a = (l1 * l1 - l2 * l2 + dd * dd) / (2 * dd)
    h = math.sqrt(max(0.0, l1 * l1 - a * a))
    px, py = -uy, ux
    joint = (root[0] + ux * a + px * h * bend, root[1] + uy * a + py * h * bend)
    end = (root[0] + ux * dd, root[1] + uy * dd)
    return joint, end


def fk(root, a1, a2, l1, l2):
    u, v = d(a1), d(a2)
    joint = (root[0] + l1 * u[0], root[1] + l1 * u[1])
    end = (joint[0] + l2 * v[0], joint[1] + l2 * v[1])
    cross = (end[0] - root[0]) * (joint[1] - root[1]) - (end[1] - root[1]) * (joint[0] - root[0])
    return end, (1 if cross >= 0 else -1)


def P(hip=None, shoulder=None, torso=90.0, tilt=0.0,
      an=None, af=None, ln=None, lf=None, fn=0.0, ff=0.0):
    """Monta uma pose. Cada membro: ('ik', x, y, bend) | ('fk', a1, a2) | ('head', dx, dy, bend)."""
    if hip is None:
        t = d(torso)
        hip = (shoulder[0] - TORSO * t[0], shoulder[1] - TORSO * t[1])
    t = d(torso)
    sh = (hip[0] + TORSO * t[0], hip[1] + TORSO * t[1])
    hc = d(torso + tilt)
    head = (sh[0] + (NECK + HR) * hc[0], sh[1] + (NECK + HR) * hc[1])

    def limb(root, spec, l1, l2):
        if spec[0] == 'ik':
            return spec[1], spec[2], spec[3]
        if spec[0] == 'head':
            return head[0] + spec[1], head[1] + spec[2], spec[3]
        end, b = fk(root, spec[1], spec[2], l1, l2)
        return end[0], end[1], b

    arm_n, arm_f = limb(sh, an, UA, FA), limb(sh, af, UA, FA)
    leg_n, leg_f = limb(hip, ln, TH, SH), limb(hip, lf, TH, SH)
    return [hip[0], hip[1], torso, tilt, *arm_n, *arm_f, *leg_n, *leg_f, fn, ff]


def skeleton(p):
    hip = (p[0], p[1])
    t = d(p[2])
    sh = (hip[0] + TORSO * t[0], hip[1] + TORSO * t[1])
    hc = d(p[2] + p[3])
    head = (sh[0] + (NECK + HR) * hc[0], sh[1] + (NECK + HR) * hc[1])
    neck = (sh[0] + NECK * hc[0], sh[1] + NECK * hc[1])
    an = ik(sh, (p[4], p[5]), UA, FA, p[6])
    af = ik(sh, (p[7], p[8]), UA, FA, p[9])
    ln = ik(hip, (p[10], p[11]), TH, SH, p[12])
    lf = ik(hip, (p[13], p[14]), TH, SH, p[15])
    fn = (ln[1][0] + FOOT * d(p[16])[0], ln[1][1] + FOOT * d(p[16])[1])
    ff = (lf[1][0] + FOOT * d(p[17])[0], lf[1][1] + FOOT * d(p[17])[1])
    return dict(hip=hip, sh=sh, neck=neck, head=head, an=an, af=af, ln=ln, lf=lf, fn=fn, ff=ff)


# ---------------------------------------------------------------- exercícios
# props: ('rect', x0, y0, x1, y1) | ('line', x0, y0, x1, y1)
EX = {}


def ex(key, a, b, props=()):
    EX[key] = (a, b, list(props))


def stand(hx=45, hy=30.4, torso=90.0, an=('fk', -92, -90), af=('fk', -88, -90), nx=45, fx=48, **kw):
    return P(hip=(hx, hy), torso=torso, an=an, af=af,
             ln=('ik', nx, ANK, 1), lf=('ik', fx, ANK, 1), **kw)


# agachamento
ex('squat',
   stand(),
   P(hip=(31, 19), torso=60, an=('fk', 4, 0), af=('fk', 8, 4),
     ln=('ik', 45, ANK, 1), lf=('ik', 48, ANK, 1)))

# afundo
ex('lunge',
   stand(nx=50, fx=42),
   P(hip=(43, 17.5), torso=90, an=('fk', -95, -92), af=('fk', -85, -88),
     ln=('ik', 58, ANK, 1), lf=('ik', 25, 5.5, 1), ff=-55))

# agachamento búlgaro (pé de trás na cadeira)
ex('bulgarian',
   P(hip=(46, 30.5), torso=90, an=('fk', -95, -92), af=('fk', -85, -88),
     ln=('ik', 50, ANK, 1), lf=('ik', 17, 21.5, 1), ff=180),
   P(hip=(45, 17), torso=82, an=('fk', -95, -92), af=('fk', -85, -88),
     ln=('ik', 52, ANK, 1), lf=('ik', 17, 21.5, 1), ff=180),
   [('rect', 8, 0, 23, 20)])

# polichinelo (vista frontal)
ex('jack',
   P(hip=(50, 30.4), torso=90, an=('fk', -100, -98), af=('fk', -80, -82),
     ln=('ik', 47, ANK, 1), lf=('ik', 53, ANK, -1)),
   P(hip=(50, 29), torso=90, an=('fk', 140, 125), af=('fk', 40, 55),
     ln=('ik', 33, ANK, 1), lf=('ik', 67, ANK, -1)))

# elevação de joelhos
ex('highknee',
   P(hip=(45, 30.6), torso=90, an=('fk', -125, -170), af=('fk', -25, 60),
     ln=('ik', 49, 14, 1), lf=('ik', 43, ANK, 1), fn=-30),
   P(hip=(45, 30.6), torso=90, an=('fk', -25, 60), af=('fk', -125, -170),
     ln=('ik', 43, ANK, 1), lf=('ik', 49, 14, 1), ff=-30))


# flexão de braços e variantes (corpo reto apoiado nos pés)
def plank_body(ankle_x, theta, hand_x, hand_y=0.0, bend=-1, an=None, af=None, head_tilt=-6):
    legs = d(theta)
    hip = (ankle_x + 30 * legs[0], ANK + 30 * legs[1])
    return P(hip=hip, torso=theta, tilt=head_tilt,
             an=an or ('ik', hand_x, hand_y, bend), af=af or ('ik', hand_x + 2, hand_y, bend),
             ln=('ik', ankle_x, ANK, 1), lf=('ik', ankle_x + 2, ANK, 1), fn=-70, ff=-70)


ex('pushup',
   plank_body(12, 25, 59),
   plank_body(12, 11, 59))

# flexão com joelhos apoiados
def knee_push(theta, hand_x, bend=-1):
    k = (24, 2.0)
    hip = (k[0] + TH * d(theta)[0], k[1] + TH * d(theta)[1])
    return P(hip=hip, torso=theta, tilt=-6,
             an=('ik', hand_x, 0, bend), af=('ik', hand_x + 2, 0, bend),
             ln=('ik', k[0] - 15, 2.0, 1), lf=('ik', k[0] - 15, 2.0, 1), fn=180, ff=180)


ex('kneepush', knee_push(34, 55), knee_push(14, 55))

# prancha
def forearm_plank(theta, head_tilt=-4):
    legs = d(theta)
    hip = (10 + 30 * legs[0], ANK + 30 * legs[1])
    return P(hip=hip, torso=theta, tilt=head_tilt,
             an=('ik', 73, 1.2, -1), af=('ik', 75, 1.2, -1),
             ln=('ik', 10, ANK, 1), lf=('ik', 12, ANK, 1), fn=-70, ff=-70)


ex('plank', forearm_plank(12), forearm_plank(14.5))

# prancha lateral (vista frontal, apoio no antebraço)
def side_plank(theta, sag):
    legs = d(theta)
    hip = (12 + 30 * legs[0], ANK + 30 * legs[1] - sag)
    tor = theta + sag * 1.4
    return P(hip=hip, torso=tor, tilt=-4,
             an=('ik', 68, 1.2, -1), af=('fk', 80, 85),
             ln=('ik', 12, ANK, 1), lf=('ik', 12, ANK, 1), fn=-70, ff=-70)


ex('sideplank', side_plank(13, 7), side_plank(13, 0))

# escalador
def climber(near_in):
    theta = 25
    sh = (60, 22.5)
    t = d(theta)
    hip = (sh[0] - TORSO * t[0], sh[1] - TORSO * t[1])
    back = ('ik', hip[0] - 27.5, ANK, 1)
    knee_in = ('ik', hip[0] + 7, 6.5, 1)
    return P(hip=hip, torso=theta, tilt=-6,
             an=('ik', 60, 0, -1), af=('ik', 62, 0, -1),
             ln=knee_in if near_in else back, lf=back if near_in else knee_in,
             fn=-55 if near_in else -70, ff=-70 if near_in else -55)


ex('climber', climber(True), climber(False))

# toque no ombro (prancha alta)
def shoulder_tap(lift):
    p = plank_body(12, 25, 59, an=('ik', 59, 0, -1) if not lift else ('ik', 67, 17, 1),
                   af=('ik', 61, 0, -1))
    return p


ex('shouldertap', shoulder_tap(False), shoulder_tap(True))

# superman (de bruços)
ex('superman',
   P(hip=(37, 4), torso=2, tilt=0, an=('fk', 2, 0), af=('fk', 0, -2),
     ln=('fk', 182, 180), lf=('fk', 178, 180), fn=-88, ff=-88),
   P(hip=(37, 4.5), torso=14, tilt=6, an=('fk', 20, 14), af=('fk', 24, 18),
     ln=('fk', 166, 163), lf=('fk', 160, 158), fn=-105, ff=-105))

# mergulho na cadeira
def dip(shoulder, hip_tor=100, bend=-1):
    return P(shoulder=shoulder, torso=hip_tor, tilt=8,
             an=('ik', 27, 19.5, bend), af=('ik', 29, 19.5, bend),
             ln=('ik', 58, ANK, 1), lf=('ik', 61, ANK, 1))


ex('dip', dip((31, 38)), dip((31, 26)), [('rect', 8, 0, 27, 19)])

# flexão pike
ex('pike',
   P(hip=(29, 27), torso=-14.6, tilt=-30,
     an=('ik', 54, 0, 1), af=('ik', 56, 0, 1),
     ln=('ik', 14, ANK, 1), lf=('ik', 16, ANK, 1), fn=-70, ff=-70),
   P(hip=(29, 27), torso=-46, tilt=-35,
     an=('ik', 54, 0, 1), af=('ik', 56, 0, 1),
     ln=('ik', 14, ANK, 1), lf=('ik', 16, ANK, 1), fn=-70, ff=-70))

# abdominal (de costas, cabeça à esquerda)
ex('crunch',
   P(hip=(50, 4), torso=180, tilt=0,
     an=('head', -1, 3, 1), af=('head', 1, 4, 1),
     ln=('ik', 66, ANK, 1), lf=('ik', 68, ANK, 1), fn=0, ff=0),
   P(hip=(50, 4), torso=140, tilt=10,
     an=('head', -1, 3, 1), af=('head', 1, 4, 1),
     ln=('ik', 66, ANK, 1), lf=('ik', 68, ANK, 1), fn=0, ff=0))

# elevação de pernas (de costas)
ex('legraise',
   P(hip=(42, 4), torso=180, tilt=0, an=('fk', -4, -4), af=('fk', -2, -2),
     ln=('fk', 88, 88), lf=('fk', 90, 90), fn=178, ff=180),
   P(hip=(42, 4), torso=180, tilt=0, an=('fk', -4, -4), af=('fk', -2, -2),
     ln=('fk', 10, 8), lf=('fk', 12, 10), fn=100, ff=100))

# elevação de quadril / ponte
def bridge(tor):
    sh = (15, 5.0)
    return P(shoulder=sh, torso=tor, tilt=-(tor - 180),
             an=('fk', -4, -4), af=('fk', -2, -2),
             ln=('ik', 53, ANK, 1), lf=('ik', 55, ANK, 1), fn=0, ff=0)


ex('bridge', bridge(184), bridge(213))

# panturrilha em pé
ex('calf',
   P(hip=(45, 30.4), torso=90, an=('fk', -92, -90), af=('fk', -88, -90),
     ln=('ik', 45, ANK, 1), lf=('ik', 48, ANK, 1)),
   P(hip=(45, 34.4), torso=90, an=('fk', -92, -90), af=('fk', -88, -90),
     ln=('ik', 45, 5.4, 1), lf=('ik', 48, 5.4, 1), fn=-48, ff=-48))

# agachamento na parede
ex('wallsit',
   P(hip=(30, 30.4), torso=90, an=('fk', -92, -90), af=('fk', -88, -90),
     ln=('ik', 31, ANK, 1), lf=('ik', 33, ANK, 1)),
   P(hip=(30, 16), torso=90, an=('fk', 10, 0), af=('fk', 14, 4),
     ln=('ik', 45, ANK, 1), lf=('ik', 47, ANK, 1)),
   [('rect', 22, 0, 26, 70)])

# remada invertida sob a mesa (cabeça à esquerda)
def row(sh, tor):
    return P(shoulder=sh, torso=tor, tilt=-(tor - 180) - 4,
             an=('ik', 29, 27, -1), af=('ik', 31, 27, -1),
             ln=('ik', 78, ANK, 1), lf=('ik', 80, ANK, 1), fn=95, ff=95)


ex('row', row((26, 4), 177), row((30, 19), 160),
   [('rect', 12, 27, 92, 31), ('rect', 14, 0, 18, 27), ('rect', 86, 0, 90, 27)])

# prancha invertida
ex('revplank',
   P(hip=(41, 8), torso=140.5, tilt=18,
     an=('ik', 23, 0, -1), af=('ik', 21, 0, -1),
     ln=('ik', 70, ANK, 1), lf=('ik', 72, ANK, 1), fn=90, ff=90),
   P(hip=(42.7, 13.6), torso=155.7, tilt=18,
     an=('ik', 23, 0, -1), af=('ik', 21, 0, -1),
     ln=('ik', 70, ANK, 1), lf=('ik', 72, ANK, 1), fn=90, ff=90))


# ---- variações mais difíceis / novos exercícios

# flexão com pés elevados (cadeira)
def feet_push(theta, hand_x=61):
    ankle = (10, 17.2)
    legs = d(theta)
    hip = (ankle[0] + 30 * legs[0], ankle[1] + 30 * legs[1])
    return P(hip=hip, torso=theta, tilt=-8,
             an=('ik', hand_x, 0, -1), af=('ik', hand_x + 2, 0, -1),
             ln=('ik', ankle[0], ankle[1], 1), lf=('ik', ankle[0] + 2, ankle[1], 1), fn=-50, ff=-50)


ex('pushup_feet', feet_push(6), feet_push(-8), [('rect', 2, 0, 16, 16)])

# pike com pés elevados
ex('pike_feet',
   P(hip=(34.4, 38), torso=-44.8, tilt=-30,
     an=('ik', 52, 0, 1), af=('ik', 54, 0, 1),
     ln=('ik', 14, 17.2, 1), lf=('ik', 16, 17.2, 1), fn=-50, ff=-50),
   P(hip=(34.4, 38), torso=-75, tilt=-35,
     an=('ik', 52, 0, 1), af=('ik', 54, 0, 1),
     ln=('ik', 14, 17.2, 1), lf=('ik', 16, 17.2, 1), fn=-50, ff=-50),
   [('rect', 6, 0, 20, 16)])

# agachamento com salto
ex('jumpsquat',
   P(hip=(31, 19), torso=60, an=('fk', 4, 0), af=('fk', 8, 4),
     ln=('ik', 45, ANK, 1), lf=('ik', 48, ANK, 1)),
   P(hip=(45, 33.5), torso=90, an=('fk', 35, 25), af=('fk', 40, 30),
     ln=('ik', 45, 7.5, 1), lf=('ik', 48, 7.5, 1), fn=-45, ff=-45))

# ponte com uma perna
def bridge1(tor):
    sh = (15, 5.0)
    return P(shoulder=sh, torso=tor, tilt=-(tor - 180),
             an=('fk', -4, -4), af=('fk', -2, -2),
             ln=('ik', 53, ANK, 1), lf=('fk', 40, 40), fn=0, ff=-50)


ex('bridge1', bridge1(184), bridge1(213))

# abdominal bicicleta (de costas, cabeça à esquerda)
def bicycle(near_in):
    tuck = ('ik', 54, 19, 1)
    reach = ('fk', 10, 8)
    return P(hip=(50, 4), torso=160, tilt=10,
             an=('head', -1, 3, 1), af=('head', 1, 4, 1),
             ln=tuck if near_in else reach, lf=reach if near_in else tuck,
             fn=0 if near_in else 98, ff=98 if near_in else 0)


ex('bicycle', bicycle(True), bicycle(False))

# tesoura (pernas alternadas, de costas)
ex('flutter',
   P(hip=(42, 4), torso=180, tilt=0, an=('fk', -4, -4), af=('fk', -2, -2),
     ln=('fk', 32, 30), lf=('fk', 8, 6), fn=120, ff=98),
   P(hip=(42, 4), torso=180, tilt=0, an=('fk', -4, -4), af=('fk', -2, -2),
     ln=('fk', 8, 6), lf=('fk', 32, 30), fn=98, ff=120))


# ---------------------------------------------------------------- saída
def fmt(v):
    s = ('%.2f' % v).rstrip('0').rstrip('.')
    return (s if s not in ('', '-0') else '0') + 'f'


def harmonize(a, b):
    """Se o sinal de dobra diverge, usa o da pose em que o membro está mais dobrado."""
    roots = {6: (4, 5, 'sh', UA + FA), 9: (7, 8, 'sh', UA + FA), 12: (10, 11, 'hip', TH + SH), 15: (13, 14, 'hip', TH + SH)}
    ska, skb = skeleton(a), skeleton(b)
    for i, (tx, ty, root, total) in roots.items():
        if a[i] != b[i]:
            da = math.hypot(a[tx] - ska[root][0], a[ty] - ska[root][1]) / total
            db = math.hypot(b[tx] - skb[root][0], b[ty] - skb[root][1]) / total
            sign = a[i] if da <= db else b[i]
            a[i] = sign
            b[i] = sign


def to_kotlin(path):
    for _k, (_a, _b, _p) in EX.items():
        harmonize(_a, _b)
    lines = [
        '// ARQUIVO GERADO por tools/gen_poses.py. Para ajustar uma animação, edite o script e gere de novo.',
        'package com.yourlife.app.data',
        '',
        'val ANIM_GEOMETRY: Map<String, AnimGeometry> = mapOf(',
    ]
    for key, (a, b, props) in EX.items():
        pa = ', '.join(fmt(v) for v in a)
        pb = ', '.join(fmt(v) for v in b)
        pr = ', '.join(
            'Prop(%d, %s, %s, %s, %s)' % (0 if p[0] == 'rect' else 1, fmt(p[1]), fmt(p[2]), fmt(p[3]), fmt(p[4]))
            for p in props)
        lines.append('    "%s" to AnimGeometry(' % key)
        lines.append('        a = floatArrayOf(%s),' % pa)
        lines.append('        b = floatArrayOf(%s),' % pb)
        lines.append('        props = listOf(%s)' % pr)
        lines.append('    ),')
    lines.append(')')
    with open(path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines) + '\n')
    for k, (a, b, _) in EX.items():
        for i in (6, 9, 12, 15):
            if a[i] != b[i]:
                print('AVISO: sinal de dobra diferente em', k, i)


def preview(outdir):
    for _k, (_a, _b, _p) in EX.items():
        harmonize(_a, _b)
    from PIL import Image, ImageDraw
    import os
    os.makedirs(outdir, exist_ok=True)
    S = 3.2
    W, H = int(100 * S), int(75 * S)
    keys = list(EX.keys())
    cols = 4
    per_sheet = 12
    for sheet in range((len(keys) + per_sheet - 1) // per_sheet):
        chunk = keys[sheet * per_sheet:(sheet + 1) * per_sheet]
        rows = (len(chunk) + cols - 1) // cols
        img = Image.new('RGB', (cols * (W * 2 + 14), rows * (H + 26)), 'white')
        dr = ImageDraw.Draw(img)
        for idx, key in enumerate(chunk):
            a, b, props = EX[key]
            ox = (idx % cols) * (W * 2 + 14)
            oy = (idx // cols) * (H + 26)
            dr.text((ox + 4, oy + 4), key, fill='black')
            for j, pose in enumerate((a, b)):
                bx, by = ox + j * (W + 7), oy + 20
                dr.rectangle((bx, by, bx + W, by + H), outline='#cccccc', fill='#f6f6f8')
                gy = by + H - 6 * S

                def pt(p):
                    return (bx + p[0] * S, gy - p[1] * S)

                dr.line((bx, gy, bx + W, gy), fill='#999999', width=2)
                for pr in props:
                    if pr[0] == 'rect':
                        dr.rectangle((bx + pr[1] * S, gy - pr[4] * S, bx + pr[3] * S, gy - pr[2] * S), fill='#b8b8c0')
                sk = skeleton(pose)
                col_f, col_n = '#9fb0e8', '#2b3fd0'
                for limb, lcol in (('af', col_f), ('lf', col_f)):
                    root = sk['sh'] if limb == 'af' else sk['hip']
                    j, e = sk[limb]
                    dr.line((*pt(root), *pt(j), *pt(e)), fill=lcol, width=int(3.2 * S), joint='curve')
                dr.line((*pt(sk['hip']), *pt(sk['sh'])), fill=col_n, width=int(4 * S))
                for foot, ank in (('ff', 'lf'), ('fn', 'ln')):
                    dr.line((*pt(sk[ank][1]), *pt(sk[foot])), fill=col_f if foot == 'ff' else col_n, width=int(3 * S))
                for limb in ('ln', 'an'):
                    root = sk['sh'] if limb == 'an' else sk['hip']
                    j, e = sk[limb]
                    dr.line((*pt(root), *pt(j), *pt(e)), fill=col_n, width=int(3.2 * S), joint='curve')
                hx, hy = pt(sk['head'])
                r = HR * S
                dr.ellipse((hx - r, hy - r, hx + r, hy + r), fill=col_n)
        img.save(os.path.join(outdir, 'sheet%d.png' % (sheet + 1)))
    print('ok', len(keys), 'exercícios')


if __name__ == '__main__':
    mode = sys.argv[1]
    if mode == 'kotlin':
        to_kotlin(sys.argv[2])
    elif mode == 'preview':
        preview(sys.argv[2])
