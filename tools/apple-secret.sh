#!/bin/zsh
# Sign in with Apple client secret for Supabase: ES256 JWT, valid ~180 days, signed with
# ~/keys/chroma-siwa.p8 (key Y275S5BQZ8). Prints it to stdout: pipe it, never echo it.
# Renewal: store/servidor.md 4.
b64() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }
now=$(date +%s)
h=$(printf '{"alg":"ES256","kid":"Y275S5BQZ8","typ":"JWT"}' | b64)
c=$(printf '{"iss":"BR5ZH8XYP6","iat":%d,"exp":%d,"aud":"https://appleid.apple.com","sub":"com.baltajmn.color.signin"}' $now $((now + 15552000)) | b64)
s=$(printf '%s.%s' "$h" "$c" | openssl dgst -sha256 -sign ~/keys/chroma-siwa.p8 | python3 -c 'import sys
d = sys.stdin.buffer.read()
r = d[4:4 + d[3]]; s = d[6 + d[3]:]
sys.stdout.buffer.write(r[-32:].rjust(32, b"\0") + s[-32:].rjust(32, b"\0"))' | b64)
printf '%s.%s.%s' "$h" "$c" "$s"
