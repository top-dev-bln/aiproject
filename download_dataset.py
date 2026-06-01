import os
import subprocess
from github import Github

GITHUB_TOKEN = "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"
QUERY = "language:java stars:>30 size:<500 archived:false topic:spring-boot,rest-api,microservice"
NUMAR_PROIECTE = 200
START_INDEX = 0

DIRECTOR_DESTINATIE = "dataset_brut"
# -------------------

def main():
    print("Autentificare la GitHub...")
    g = Github(GITHUB_TOKEN)

    if not os.path.exists(DIRECTOR_DESTINATIE):
        os.makedirs(DIRECTOR_DESTINATIE)

    print(f"Cautam proiecte cu query-ul: {QUERY}\n")
    repositories = g.search_repositories(query=QUERY)

    repo_slice = repositories[START_INDEX : START_INDEX + NUMAR_PROIECTE]

    count = 0
    for repo in repo_slice:
        nume_repo = repo.name
        clone_url = repo.clone_url
        cale_locala = os.path.join(DIRECTOR_DESTINATIE, nume_repo)

        if os.path.exists(cale_locala):
            print(f"[{count+1}/{NUMAR_PROIECTE}] Se sare peste '{nume_repo}' - exista deja.")
            count += 1
            continue

        print(f"[{count+1}/{NUMAR_PROIECTE}] Se descarca: {repo.full_name} ({repo.stargazers_count} stars)")

        comanda_clone = ["git", "clone", "--depth", "1", clone_url, cale_locala]

        try:
            subprocess.run(
                comanda_clone, 
                check=True, 
                stdout=subprocess.DEVNULL, 
                stderr=subprocess.DEVNULL
            )
            print("    -> Succes")
        except subprocess.CalledProcessError:
            print("    -> [!] EROARE la clonare. Posibil timeout sau lipsa permisiuni.")

        count += 1

    print("\n[✔] Descarcare completata. Repozitoarele sunt in folderul:", DIRECTOR_DESTINATIE)

if __name__ == "__main__":
    main()