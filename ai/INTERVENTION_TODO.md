# Intervention Agent TODO

## Current Implementation Status

### ✅ Completed
- [x] Basic intervention agent graph structure ([intervention_agent.py](src/agent/intervention_agent.py))
- [x] 4-node workflow: `analyze_behavior` → `decide_intervention` → `generate_nudge` → `send_intervention`
- [x] State management with `InterventionState` TypedDict ([dito_agent.py](src/agent/dito_agent.py))
- [x] Structured LLM outputs using Pydantic schemas
- [x] Conditional routing with Command pattern
- [x] Basic test function for manual testing
- [x] Simulation functions for MVP testing

---

## 🚧 TODO: Core Functionality

### 1. Intervention Frequency Control (Rate Limiting)
**Priority: HIGH** | **Lines: [intervention_agent.py:137-178](src/agent/intervention_agent.py#L137-L178)**

Currently, `send_intervention()` does not enforce frequency limits from guidelines.

- [ ] **1.1 Implement frequency checker function**
  - Location: [dito_agent.py](src/agent/dito_agent.py) utility functions section
  - Function: `check_intervention_frequency(user_id: int) -> dict`
  - Returns:
    ```python
    {
        "can_intervene": bool,
        "reason": str,  # "ok", "daily_limit_reached", "min_gap_not_met"
        "interventions_today": int,
        "last_intervention_time": Optional[str],
        "next_available_time": Optional[str]
    }
    ```
  - Rules to implement:
    - Max 4 interventions per day
    - Minimum 2-3 hour gap between interventions
    - Check against simulated or real DB data

- [ ] **1.2 Integrate frequency check into decide_intervention node**
  - Location: [intervention_agent.py:55-97](src/agent/intervention_agent.py#L55-L97)
  - Add frequency check before LLM decision
  - If frequency limit violated, force `intervention_needed = False`
  - Update decision reasoning to include frequency check result

- [ ] **1.3 Add intervention history tracking**
  - Update `save_intervention_to_db()` to track timestamp
  - Store in-memory cache for MVP (later replace with Redis/DB)
  - Track per-user intervention count and timestamps

### 2. Enhanced Behavior Analysis
**Priority: MEDIUM** | **Lines: [intervention_agent.py:15-52](src/agent/intervention_agent.py#L15-L52)**

Current `analyze_behavior()` uses basic pattern analysis. Need more robust detection.

- [ ] **2.1 Add multi-trigger detection**
  - Current: Analyzes single behavior log
  - Needed: Detect compound triggers (e.g., "10+ app switches in 5 min")
  - Function: `detect_trigger_events(behavior_log: dict) -> list[str]`
  - Return multiple triggers if applicable:
    ```python
    ["continuous-30min-use", "bedtime-usage"]
    ```

- [ ] **2.2 Add historical context**
  - Query user's recent behavior patterns (last 24 hours)
  - Compare current behavior against user's baseline
  - Detect anomalies (e.g., "150% more usage than usual")
  - Add to analysis prompt context

- [ ] **2.3 Implement trigger event detection logic**
  - Location: New function in [dito_agent.py](src/agent/dito_agent.py)
  - Function: `calculate_trigger_metrics(behavior_log: dict) -> dict`
  - Metrics to calculate:
    ```python
    {
        "continuous_use_minutes": int,
        "app_switches_last_5min": int,
        "repeated_launches_last_5min": int,
        "short_form_duration_minutes": int,
        "is_bedtime_usage": bool,  # within 30 min of typical bedtime
    }
    ```

### 3. Error Handling & Resilience
**Priority: HIGH** | **Lines: All node functions**

Current implementation has no error handling for LLM failures or invalid states.

- [ ] **3.1 Add try-except blocks to all node functions**
  - Wrap LLM invocations in try-except
  - Handle `RateLimitError`, `APIConnectionError`, `APIError`
  - Return graceful degradation responses
  - Example:
    ```python
    try:
        analysis = behavior_analyzer.invoke([...])
    except RateLimitError:
        # Return safe default or retry with backoff
        return {"behavior_pattern": "analysis_failed", ...}
    ```

- [ ] **3.2 Add state validation**
  - Validate required fields at each node entry
  - Function: `validate_intervention_state(state: InterventionState, step: str) -> None`
  - Raise `ValueError` if critical fields missing
  - Log validation errors

- [ ] **3.3 Add LLM response validation**
  - Verify structured output contains expected fields
  - Handle cases where LLM returns invalid enum values
  - Add fallback logic for malformed responses

### 4. Logging & Observability
**Priority: MEDIUM** | **Lines: All functions**

Current implementation uses basic `print()` statements.

- [ ] **4.1 Replace print statements with structured logging**
  - Use Python `logging` module
  - Create logger: `logger = logging.getLogger(__name__)`
  - Log levels:
    - `INFO`: Workflow progress, decisions made
    - `DEBUG`: LLM prompts and responses
    - `WARNING`: Frequency limits hit, validation issues
    - `ERROR`: LLM failures, exceptions
  - Example:
    ```python
    logger.info(f"[analyze_behavior] Pattern: {analysis.pattern_type}, Trigger: {analysis.trigger_event}")
    ```

- [ ] **4.2 Add LangSmith tracing metadata**
  - Add tags to identify intervention type
  - Add metadata for user_id, session_id
  - Example in config:
    ```python
    config = {
        "configurable": {"thread_id": f"user_{user_id}"},
        "tags": ["intervention", intervention_type],
        "metadata": {"user_id": user_id, "session_id": session_id}
    }
    ```

- [ ] **4.3 Add performance timing**
  - Track execution time for each node
  - Log slow LLM calls (>5 seconds)
  - Add timing decorator:
    ```python
    @log_execution_time
    def analyze_behavior(state: InterventionState) -> dict:
        ...
    ```

### 5. Nudge Message Quality
**Priority: MEDIUM** | **Lines: [intervention_agent.py:100-134](src/agent/intervention_agent.py#L100-L134)**

Current `generate_nudge()` uses basic prompt. Need more sophisticated message generation.

- [ ] **5.1 Add nudge templates by intervention type**
  - Create template library in [dito_agent.py](src/agent/dito_agent.py)
  - Templates for each intervention type:
    ```python
    NUDGE_TEMPLATES = {
        "short-form-overuse": {
            "recognition": "You've been watching short videos for {duration} minutes",
            "suggestion": "How about a 5-minute break to stretch?",
            "reward": "Complete this and earn +5 coins!"
        },
        "bedtime-usage": {
            "recognition": "It's {time}, close to your usual bedtime",
            "suggestion": "Consider winding down for better sleep",
            "reward": "Put down your phone now for +10 coins tomorrow"
        },
        ...
    }
    ```
  - Fill templates with context-specific values

- [ ] **5.2 Add personalization parameters**
  - Pass user preferences to nudge generation
  - User's coin balance, favorite alternative activities
  - Tone preference (friendly, firm, humorous)
  - Add to prompt context

- [ ] **5.3 Add A/B testing framework**
  - Generate multiple nudge variants
  - Track which variant was sent
  - Prepare for effectiveness comparison in evaluation

### 6. Database Integration Preparation
**Priority: MEDIUM** | **Lines: [dito_agent.py:204-241](src/agent/dito_agent.py#L204-L241)**

Currently using simulation functions. Prepare for real DB integration.

- [ ] **6.1 Define database client interface**
  - Create `database_client.py` in `src/agent/`
  - Abstract class or protocol:
    ```python
    class DatabaseClient(Protocol):
        def fetch_behavior_log(self, user_id: int, session_id: str) -> dict: ...
        def fetch_recent_interventions(self, user_id: int, hours: int) -> list[dict]: ...
        def save_intervention(self, intervention_data: dict) -> int: ...
        def fetch_user_preferences(self, user_id: int) -> dict: ...
    ```

- [ ] **6.2 Create simulation client implementation**
  - Implement `SimulationDatabaseClient` class
  - Move simulation functions into class methods
  - Keep current behavior for MVP testing

- [ ] **6.3 Prepare for production client**
  - Create placeholder `ProductionDatabaseClient` class
  - Add TODO comments for HTTP client integration
  - Environment variable to switch: `DB_CLIENT_MODE=simulation|production`

### 7. Config Validation
**Priority: LOW** | **Lines: [dito_agent.py:150-166](src/agent/dito_agent.py#L150-L166)**

Ensure all required environment variables are set.

- [ ] **7.1 Add environment validation on module load**
  - Check `ANTHROPIC_API_KEY` exists
  - Check `LANGSMITH_API_KEY` if tracing enabled
  - Function: `validate_environment() -> None`
  - Raise clear error messages if missing

- [ ] **7.2 Add config dataclass**
  - Create `InterventionConfig` Pydantic model
  - Load from environment variables
  - Validate ranges (e.g., max interventions per day 1-10)
  - Example:
    ```python
    class InterventionConfig(BaseModel):
        max_interventions_per_day: int = Field(default=4, ge=1, le=10)
        min_gap_hours: float = Field(default=2.0, ge=0.5, le=12.0)
        evaluation_delay_minutes: int = Field(default=45, ge=15, le=120)
    ```

### 8. Testing Improvements
**Priority: HIGH** | **Lines: [intervention_agent.py:217-253](src/agent/intervention_agent.py#L217-L253)**

Current test function is basic. Need comprehensive test coverage.

- [ ] **8.1 Add parameterized test scenarios**
  - Different behavior log scenarios:
    - Short-form overuse (20+ minutes)
    - Bedtime usage (30 min before sleep)
    - App switching (10+ switches in 5 min)
    - Normal usage (no intervention needed)
  - Function: `get_test_scenario(scenario_name: str) -> dict`

- [ ] **8.2 Create assertion helpers**
  - Verify expected workflow paths taken
  - Assert intervention_needed = True/False correctly
  - Validate nudge message format
  - Check evaluation scheduling

- [ ] **8.3 Add mock LLM responses for deterministic testing**
  - Create mock analyzer that returns fixed responses
  - Test conditional routing paths
  - Test error handling paths

- [ ] **8.4 Add integration test with LangGraph Studio**
  - Document how to visualize workflow in LangGraph Studio
  - Add example thread_id for debugging
  - Test with LangSmith tracing enabled

### 9. Intervention Scheduling Logic
**Priority: MEDIUM** | **Lines: [intervention_agent.py:137-178](src/agent/intervention_agent.py#L137-L178)**

Current implementation calculates delay but doesn't actually schedule.

- [ ] **9.1 Add scheduling interface**
  - Abstract class for scheduling evaluation jobs
  - Protocol:
    ```python
    class EvaluationScheduler(Protocol):
        def schedule_evaluation(
            self,
            intervention_id: int,
            scheduled_time: str,
            user_id: int
        ) -> str:  # Returns job_id
            ...
    ```

- [ ] **9.2 Create in-memory scheduler for MVP**
  - Simple queue-based scheduler using `heapq` or `schedule` library
  - Store scheduled evaluations in memory
  - Background thread to check and trigger evaluations

- [ ] **9.3 Prepare for production scheduler**
  - Placeholder for Celery/RQ/APScheduler integration
  - Document integration points
  - Add TODO for external job queue

### 10. Prompt Engineering Refinements
**Priority: LOW** | **Lines: All LLM invocation points**

Improve prompt quality for better LLM decisions.

- [ ] **10.1 Add few-shot examples to prompts**
  - Add 2-3 examples per prompt
  - Show desired reasoning patterns
  - Example:
    ```python
    analysis_prompt = f"""
    Example 1:
    User watched YouTube Shorts for 25 minutes continuously at 11 PM.
    Pattern: critical, Trigger: short-form-overuse + bedtime-usage

    Example 2:
    User switched between apps 15 times in 5 minutes at 2 PM.
    Pattern: concerning, Trigger: app-switching

    Now analyze:
    - App: {behavior_log['app_name']}
    - Duration: {behavior_log['duration_seconds']}s
    ...
    ```

- [ ] **10.2 Add chain-of-thought prompting**
  - Ask LLM to explain reasoning step-by-step
  - Extract reasoning into separate field
  - Use for debugging and transparency

- [ ] **10.3 Optimize prompt length**
  - Remove redundant context
  - Use concise guidelines summary
  - Benchmark token usage before/after

---

## 📋 Implementation Priority

### Phase 1: Stability & Reliability (Week 1)
1. Error handling & resilience (TODO #3)
2. Intervention frequency control (TODO #1)
3. Testing improvements (TODO #8)
4. Logging & observability (TODO #4)

### Phase 2: Enhanced Logic (Week 2)
5. Enhanced behavior analysis (TODO #2)
6. Nudge message quality (TODO #5)
7. Config validation (TODO #7)

### Phase 3: Production Readiness (Week 3)
8. Database integration preparation (TODO #6)
9. Intervention scheduling logic (TODO #9)
10. Prompt engineering refinements (TODO #10)

---

## 🧪 Testing Checklist

### Manual Testing
- [ ] Test with behavior log triggering intervention
- [ ] Test with behavior log NOT triggering intervention
- [ ] Test frequency limit enforcement (4th intervention should be blocked)
- [ ] Test minimum gap enforcement (intervention within 2 hours should be blocked)
- [ ] Test LLM failure scenario (disconnect network)
- [ ] Test with invalid state (missing required fields)

### Automated Testing
- [ ] Unit test for `analyze_behavior()` with mocked LLM
- [ ] Unit test for `decide_intervention()` conditional routing
- [ ] Unit test for `generate_nudge()` message generation
- [ ] Unit test for `send_intervention()` scheduling logic
- [ ] Integration test for complete graph execution (intervention needed path)
- [ ] Integration test for complete graph execution (no intervention path)
- [ ] Integration test for frequency limit edge cases

### LangGraph Server Testing
- [ ] Test via LangGraph Studio visualization
- [ ] Test via REST API: `POST /threads/{thread_id}/runs`
- [ ] Verify state persistence between runs
- [ ] Test with LangSmith tracing enabled
- [ ] Load test with 10+ concurrent requests

---

## 📝 Documentation TODO

- [ ] Add docstrings to all node functions (Google style)
- [ ] Document expected behavior_log schema
- [ ] Document intervention_type enum values and meanings
- [ ] Create architecture diagram (Mermaid or draw.io)
- [ ] Add examples of LLM prompts and responses
- [ ] Document LangSmith tracing setup
- [ ] Add troubleshooting guide for common issues

---

## 🔧 Code Quality TODO

- [ ] Run `make lint` and fix all issues
- [ ] Run `make format` to ensure consistent style
- [ ] Add type hints to all functions (mypy strict mode)
- [ ] Remove commented-out code
- [ ] Extract magic numbers to constants
- [ ] Reduce code duplication between node functions

---

## Next Immediate Steps

1. **Implement frequency control** (TODO #1.1-1.3)
   - Start with in-memory tracking for MVP
   - Add to `decide_intervention()` node
   - Test with multiple rapid intervention attempts

2. **Add basic error handling** (TODO #3.1)
   - Wrap all LLM calls in try-except
   - Return graceful fallback responses
   - Test by simulating API failures

3. **Improve logging** (TODO #4.1)
   - Replace print with logging module
   - Set up log levels
   - Test log output clarity

4. **Add test scenarios** (TODO #8.1)
   - Create 5 test scenarios covering different triggers
   - Run each scenario and verify correct behavior
   - Document expected vs actual results
