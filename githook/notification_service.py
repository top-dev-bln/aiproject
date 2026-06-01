from user import User


class NotificationService:
    def __init__(self):
        self.sent: list[str] = []

    def notify(self, user: User, message: str) -> None:
        entry = f"[email:{user.email}] {message}"
        self.sent.append(entry)
        print(entry)

    def get_sent_count(self) -> int:
        return len(self.sent)
