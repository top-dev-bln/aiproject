#!/bin/bash
# Sterge fisierele fara pereche intre diagrame_puml/ si json_output/

PUML_DIR="diagrame_puml"
JSON_DIR="json_output"

echo "=== Verificare perechi ==="

# Sterge .puml fara .json corespondent
for puml in "$PUML_DIR"/*.puml; do
    name=$(basename "$puml" .puml)
    if [ ! -f "$JSON_DIR/$name.json" ]; then
        echo "Sterg (fara JSON): $puml"
        rm "$puml"
    fi
done

# Sterge .json fara .puml corespondent
for json in "$JSON_DIR"/*.json; do
    name=$(basename "$json" .json)
    if [ ! -f "$PUML_DIR/$name.puml" ]; then
        echo "Sterg (fara PUML): $json"
        rm "$json"
    fi
done

echo ""
echo "Dupa sync:"
echo "  PUML : $(ls -1 $PUML_DIR/*.puml | wc -l)"
echo "  JSON : $(ls -1 $JSON_DIR/*.json | wc -l)"
