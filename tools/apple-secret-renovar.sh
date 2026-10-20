#!/bin/zsh
# Renews the Sign in with Apple client secret of the shared Supabase project (store/servidor.md 4).
# Runs monthly from ~/Library/LaunchAgents/com.chroma.apple-secret.plist, without Claude.
# The secret and the Supabase token live only in shell variables and pipes: never printed, never in argv.
# Order matters: Apple must accept the new secret before Supabase gets it, so a bad one never replaces a good one.
ref=djfbmvagflfptqhabdln
client=com.baltajmn.color.signin
repo=${0:A:h:h}
doc=$repo/store/servidor.md

fail() {  # $1 what failed, $2 why, $3 what to do
  print -r -- "$(date '+%F %T') FALLO: $1 | Por que: $2 | Que hacer: $3"
  node ~/AndroidStudioProjects/TikTok/avisar.mjs "Chroma: no se renovo el secreto de Apple" "$1"$'\n'"Por que: $2"$'\n'"Que hacer: $3"
  exit 1
}
keep='El secreto que hay en Supabase sigue valiendo hasta su fecha de caducidad (store/servidor.md 4), asi que hay margen.'

secret=$($repo/tools/apple-secret.sh 2>/dev/null)
[[ $secret =~ '^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]{86}$' ]] ||
  fail "no se pudo generar el secreto" "falta o no se lee ~/keys/chroma-siwa.p8, o openssl fallo al firmar" \
    "mirar que ~/keys/chroma-siwa.p8 existe (clave Y275S5BQZ8) y probar tools/apple-secret.sh en el Mac. $keep"

apple=$(printf 'client_id=%s&client_secret=%s&grant_type=authorization_code&code=x' $client $secret |
  curl -s -m 30 --data-binary @- https://appleid.apple.com/auth/token |
  python3 -c 'import json,sys; print(json.load(sys.stdin).get("error",""))' 2>/dev/null)
case $apple in
  invalid_grant) ;;  # Apple accepted the secret and only rejected the fake code
  invalid_client) fail "Apple rechaza el secreto nuevo (invalid_client)" \
    "la clave Y275S5BQZ8 esta revocada o ~/keys/chroma-siwa.p8 ya no es esa, o cambio el Services ID $client" \
    "en developer.apple.com > Keys mirar si la clave sigue activa; si no, crear otra con Sign in with Apple, guardarla en ~/keys/chroma-siwa.p8 y cambiar el kid en tools/apple-secret.sh. No se toco Supabase. $keep" ;;
  *) fail "no se pudo comprobar el secreto contra Apple (respuesta: ${apple:-ninguna})" \
    "appleid.apple.com no contesto como se esperaba, probablemente sin red" \
    "ejecutar tools/apple-secret-renovar.sh a mano cuando haya red. No se toco Supabase. $keep" ;;
esac

token=$(security find-generic-password -s 'Supabase CLI' -a supabase -w 2>/dev/null)
[[ $token == go-keyring-base64:* ]] &&
  token=$(printf '%s' ${token#go-keyring-base64:} | python3 -c 'import base64,sys; sys.stdout.write(base64.b64decode(sys.stdin.read()).decode())')
[[ -n $token ]] ||
  fail "no hay token de la CLI de Supabase en el Llavero" \
    "el Llavero esta bloqueado o la CLI cerro sesion (servicio 'Supabase CLI', cuenta 'supabase')" \
    "abrir una terminal en el Mac y hacer 'supabase login', luego ejecutar tools/apple-secret-renovar.sh. $keep"

api=https://api.supabase.com/v1/projects/$ref/config/auth
auth() { printf 'Authorization: Bearer %s\n' $token; }
code=$(printf '{"external_apple_secret":"%s"}' $secret |
  curl -s -m 30 -o /dev/null -w '%{http_code}' -X PATCH -H @<(auth) -H 'Content-Type: application/json' --data-binary @- $api)
[[ $code == 200 ]] ||
  fail "Supabase no acepto el secreto nuevo (HTTP $code)" \
    "el token de la CLI caduco o es de otra cuenta, o la Management API fallo" \
    "hacer 'supabase login' en una terminal y ejecutar tools/apple-secret-renovar.sh. Apple si lo acepta. $keep"

# The API never gives the secret back: GET returns a 64-char value that is the same for any secret (measured
# 20-10-2026), so all that can be checked afterwards is that Apple stays enabled and the field is set.
set_ok=$(curl -s -m 30 -H @<(auth) $api | python3 -c 'import json,sys
d = json.load(sys.stdin)
print(d.get("external_apple_enabled") is True and bool(d.get("external_apple_secret")))' 2>/dev/null)
[[ $set_ok == True ]] ||
  fail "Supabase contesto 200 pero Apple quedo sin secreto o desactivado" \
    "la lectura posterior de la configuracion no muestra Apple activo con secreto" \
    "mirar Authentication > Sign In / Providers > Apple en el panel de Supabase y repetir tools/apple-secret-renovar.sh. $keep"

exp=$(print -rn -- $secret | python3 -c 'import base64,json,sys,time
p = sys.stdin.read().split(".")[1]
print(time.strftime("%d-%m-%Y", time.localtime(json.loads(base64.urlsafe_b64decode(p + "=" * (-len(p) % 4)))["exp"])))')
hoy=$(date +%d-%m-%Y)
python3 - $doc $hoy $exp <<'PY' || nodoc=1
import re, sys
doc, hoy, exp = sys.argv[1:]
text = open(doc, encoding="utf-8").read()
new, n = re.subn(r"secreto puesto el \d\d-\d\d-\d{4} caduca el \*\*\d\d-\d\d-\d{4}\*\*", f"secreto puesto el {hoy} caduca el **{exp}**", text)
if n != 1:
    sys.exit(1)
open(doc, "w", encoding="utf-8").write(new)
PY
(( nodoc )) && fail "el secreto se renovo pero no se pudo apuntar la fecha en store/servidor.md" \
  "no se encontro la frase 'secreto puesto el ... caduca el **...**' en la seccion 4" \
  "poner a mano en store/servidor.md 4 que el secreto se puso el $hoy y caduca el $exp"

# Local commit of that one file so the tree does not stay dirty every month; never pushed.
git -C $repo diff --quiet -- store/servidor.md ||
  git -C $repo commit -q -m "store: secreto de Apple renovado, caduca el $exp" \
    -m "Lo renueva solo com.chroma.apple-secret cada mes (tools/apple-secret-renovar.sh); la fecha de store/servidor.md 4 sigue al secreto vigente." -- store/servidor.md ||
  print -r -- "aviso: no se pudo hacer el commit de store/servidor.md"
print -r -- "$(date '+%F %T') OK: secreto renovado y cargado en Supabase, caduca el $exp"
