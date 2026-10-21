#!/usr/bin/env python3
"""store/events/nominacion.json to a featuring nomination in App Store Connect, as a draft
(`submitted: false`): sending it to Apple stays with the owner.

Usage: python3 tools/store/nominacion.py [--enviar]
Creates the nomination, or updates the one with the same name; the events of `events` must already be
In-App Events of the app (`appstore.py eventos`). With --enviar it submits the draft.
"""
import json, pathlib, sys

sys.path.insert(0, str(pathlib.Path.home() / "AndroidStudioProjects/ci/tienda"))
import appstore as a

ROOT = pathlib.Path(__file__).resolve().parents[2]
BUNDLE = "com.baltajmn.color"


def main():
    n = json.loads((ROOT / "store/events/nominacion.json").read_text(encoding="utf-8"))
    app = a.app_of(BUNDLE)["id"]
    por_nombre = {e["attributes"]["referenceName"]: e["id"] for e in a.get_all(f"/v1/apps/{app}/appEvents")}
    eventos = [por_nombre[json.loads((ROOT / "store/events" / d / "event.json").read_text(encoding="utf-8"))["referenceName"]] for d in n["events"]]
    attrs = {"name": n["name"], "type": n["type"], "description": n["description"], "notes": n["notes"],
             "publishStartDate": n["publishStart"], "publishEndDate": n["publishEnd"], "deviceFamilies": n["deviceFamilies"],
             "locales": n["locales"], "supplementalMaterialsUris": n["supplementalMaterials"], "hasInAppEvents": bool(eventos),
             "launchInSelectMarketsFirst": False, "preOrderEnabled": False, "submitted": "--enviar" in sys.argv}
    rel = {"relatedApps": {"data": [{"type": "apps", "id": app}]},
           "inAppEvents": {"data": [{"type": "appEvents", "id": i} for i in eventos]}}
    # Apple only takes events that are already approved: until then the nomination goes without them.
    ya = [x for estado in ("DRAFT", "SUBMITTED") for x in a.get_all(f"/v1/nominations?filter[state]={estado}") if x["attributes"]["name"] == n["name"]]

    def enviar(rel):
        if ya:
            return a.api("PATCH", f"/v1/nominations/{ya[0]['id']}", {"data": {"type": "nominations", "id": ya[0]["id"], "attributes": attrs, "relationships": rel}})
        return a.api("POST", "/v1/nominations", {"data": {"type": "nominations", "attributes": attrs, "relationships": rel}})

    try:
        r = enviar(rel)
    except SystemExit as e:
        if "inAppEvents" not in str(e):
            raise
        print("Apple no acepta aun los eventos (no estan aprobados): la nominacion va sin ellos; repetir cuando se aprueben")
        attrs["hasInAppEvents"] = False
        r = enviar({"relatedApps": rel["relatedApps"]})
    print(r["data"]["id"], r["data"]["attributes"]["state"])


if __name__ == "__main__":
    main()
