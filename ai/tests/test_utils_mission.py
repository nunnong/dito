"""
Unit tests for mission and notification utility functions
Tests the refactored functions: get_db_user_id, create_mission, send_fcm_with_mission, create_and_notify_mission
"""

from unittest.mock import Mock, patch

import httpx
import pytest
from agent.schemas import MissionData, MissionNotificationResult
from agent.utils import (
    create_and_notify_mission,
    create_mission,
    get_db_user_id,
    send_fcm_with_mission,
)


# =============================================================================
# Test get_db_user_id
# =============================================================================


def test_get_db_user_id_success():
    """Test successful user ID lookup"""
    mock_response = Mock()
    mock_response.json.return_value = {
        "data": {"profile": {"userId": 123}}
    }
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.get.return_value = mock_response

        result = get_db_user_id("test_user")

        assert result == 123
        mock_client.return_value.__enter__.return_value.get.assert_called_once()


def test_get_db_user_id_missing_user_id():
    """Test user ID lookup when userId is missing in response"""
    mock_response = Mock()
    mock_response.json.return_value = {
        "data": {"profile": {}}  # No userId
    }
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.get.return_value = mock_response

        result = get_db_user_id("test_user")

        assert result is None


def test_get_db_user_id_http_error():
    """Test user ID lookup with HTTP error"""
    mock_response = Mock()
    mock_response.status_code = 404
    mock_response.text = "Not found"

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.get.side_effect = httpx.HTTPStatusError(
            "Not found", request=Mock(), response=mock_response
        )

        result = get_db_user_id("test_user")

        assert result is None


def test_get_db_user_id_no_api_key():
    """Test user ID lookup without API key"""
    with patch.dict('os.environ', {}, clear=True):
        with patch('agent.utils.SECURITY_INTERNAL_API_KEY', None):
            result = get_db_user_id("test_user")

            assert result is None


# =============================================================================
# Test create_mission
# =============================================================================


def test_create_mission_success():
    """Test successful mission creation"""
    mission_data = MissionData(
        user_id=123,
        mission_type="REST",
        mission_text="Test mission",
        duration_seconds=300,
        target_app="YouTube"
    )

    mock_response = Mock()
    mock_response.json.return_value = {
        "data": {"missionId": "12345"}
    }
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.return_value = mock_response

        result = create_mission(mission_data)

        assert result == "12345"
        mock_client.return_value.__enter__.return_value.post.assert_called_once()


def test_create_mission_missing_mission_id():
    """Test mission creation when missionId is missing in response"""
    mission_data = MissionData(
        user_id=123,
        mission_type="REST",
        mission_text="Test mission",
        duration_seconds=300
    )

    mock_response = Mock()
    mock_response.json.return_value = {
        "data": {}  # No missionId
    }
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.return_value = mock_response

        result = create_mission(mission_data)

        assert result is None


def test_create_mission_http_error():
    """Test mission creation with HTTP error"""
    mission_data = MissionData(
        user_id=123,
        mission_type="REST",
        mission_text="Test mission",
        duration_seconds=300
    )

    mock_response = Mock()
    mock_response.status_code = 500
    mock_response.text = "Internal server error"

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.side_effect = httpx.HTTPStatusError(
            "Server error", request=Mock(), response=mock_response
        )

        result = create_mission(mission_data)

        assert result is None


# =============================================================================
# Test send_fcm_with_mission
# =============================================================================


def test_send_fcm_with_mission_success():
    """Test successful FCM notification send"""
    mock_response = Mock()
    mock_response.json.return_value = {"success": True}
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.return_value = mock_response

        result = send_fcm_with_mission("test_user", "12345", "Test message")

        assert result is True
        mock_client.return_value.__enter__.return_value.post.assert_called_once()


def test_send_fcm_with_mission_failure():
    """Test FCM notification send failure"""
    mock_response = Mock()
    mock_response.json.return_value = {"success": False, "error": "FCM error"}
    mock_response.raise_for_status = Mock()

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.return_value = mock_response

        result = send_fcm_with_mission("test_user", "12345", "Test message")

        assert result is False


def test_send_fcm_with_mission_http_error():
    """Test FCM notification send with HTTP error"""
    mock_response = Mock()
    mock_response.status_code = 503
    mock_response.text = "Service unavailable"

    with patch('httpx.Client') as mock_client:
        mock_client.return_value.__enter__.return_value.post.side_effect = httpx.HTTPStatusError(
            "Service unavailable", request=Mock(), response=mock_response
        )

        result = send_fcm_with_mission("test_user", "12345", "Test message")

        assert result is False


# =============================================================================
# Test create_and_notify_mission (Orchestrator)
# =============================================================================


def test_create_and_notify_mission_full_success():
    """Test complete success flow: user lookup -> mission creation -> FCM send"""
    state = {
        "user_id": "test_user",
        "nudge_message": "Test nudge",
        "nudge_type": "REST",
        "duration_seconds": 300,
        "behavior_log": {"app_name": "YouTube"}
    }

    with patch('agent.utils.get_db_user_id', return_value=123), \
         patch('agent.utils.create_mission', return_value="12345"), \
         patch('agent.utils.send_fcm_with_mission', return_value=True):

        result = create_and_notify_mission(state)

        assert result.success is True
        assert result.mission_id == "12345"
        assert result.fcm_sent is True
        assert result.db_user_id == 123
        assert result.error_stage is None


def test_create_and_notify_mission_user_lookup_failure():
    """Test orchestrator when user lookup fails"""
    state = {
        "user_id": "test_user",
        "nudge_message": "Test nudge"
    }

    with patch('agent.utils.get_db_user_id', return_value=None):
        result = create_and_notify_mission(state)

        assert result.success is False
        assert result.mission_id is None
        assert result.fcm_sent is False
        assert result.db_user_id is None
        assert result.error_stage == "user_lookup"


def test_create_and_notify_mission_mission_creation_failure():
    """Test orchestrator when mission creation fails"""
    state = {
        "user_id": "test_user",
        "nudge_message": "Test nudge",
        "nudge_type": "REST",
        "duration_seconds": 300
    }

    with patch('agent.utils.get_db_user_id', return_value=123), \
         patch('agent.utils.create_mission', return_value=None):

        result = create_and_notify_mission(state)

        assert result.success is False
        assert result.mission_id is None
        assert result.fcm_sent is False
        assert result.db_user_id == 123
        assert result.error_stage == "mission_create"


def test_create_and_notify_mission_fcm_failure():
    """Test orchestrator when FCM send fails (partial success)"""
    state = {
        "user_id": "test_user",
        "nudge_message": "Test nudge",
        "nudge_type": "MEDITATION",
        "duration_seconds": 600
    }

    with patch('agent.utils.get_db_user_id', return_value=123), \
         patch('agent.utils.create_mission', return_value="12345"), \
         patch('agent.utils.send_fcm_with_mission', return_value=False):

        result = create_and_notify_mission(state)

        # Partial success - mission created but FCM failed
        assert result.success is False
        assert result.mission_id == "12345"
        assert result.fcm_sent is False
        assert result.db_user_id == 123
        assert result.error_stage == "fcm_send"


def test_create_and_notify_mission_with_defaults():
    """Test orchestrator with default values for optional fields"""
    state = {
        "user_id": "test_user",
        "nudge_message": "Test nudge"
        # No nudge_type, duration_seconds, or behavior_log
    }

    with patch('agent.utils.get_db_user_id', return_value=123), \
         patch('agent.utils.create_mission', return_value="12345") as mock_create, \
         patch('agent.utils.send_fcm_with_mission', return_value=True):

        result = create_and_notify_mission(state)

        # Check that create_mission was called with default values
        call_args = mock_create.call_args[0][0]
        assert call_args.mission_type == "REST"  # Default
        assert call_args.duration_seconds == 300  # Default
        assert call_args.target_app == "All Apps"  # Default

        assert result.success is True
