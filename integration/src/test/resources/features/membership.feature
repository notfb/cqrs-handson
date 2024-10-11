Feature: Membership feature
  Scenario: Add student
    Given A running event-publisher and projection-aggregator
    When The following event are posted to the event-publisher:
      """event
      {
        "event_type": "AddStudentEvent",
        "timestamp": "2023-09-01T08:00:00Z",
        "userId": 100,
        "groupId": 1000
      }
      """
    Then The projection "group_members" for group 1000 should be:
      """json
      {"members":[100]}
      """