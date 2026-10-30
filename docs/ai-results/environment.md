# Measurement environment

- Timestamp (Asia/Ho_Chi_Minh): 2026-10-30T16:12:59.565006300+07:00[Asia/Ho_Chi_Minh]
- Source revision before measurement: f3f21907ccb18a473612a13a4eaa3aaf04a36c7b
- Java: 25.0.3+9-LTS
- Vendor: Microsoft
- VM: OpenJDK 64-Bit Server VM
- OS: Windows 11 10.0
- Architecture: amd64
- Available logical processors: 16
- Arena workers: 2; pilot and benchmark sequential.
- EASY seed: uncontrolled: production Random has no seed API; src unchanged
- Each game owns its Board and two MinimaxAI instances; heuristic methods have no mutable shared state. No engine is searched concurrently. Production Board/AI source is not modified.
- Headless tool: no Swing UI, network, audio, or user Preferences. Compilation occurs in an external temporary copy.
