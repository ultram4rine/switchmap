package ru.sgu.switchmap.models

import org.http4s.rho.swagger.models._

object Swagger {
// Auth.
  val userModel: Set[Model] = Set(
    ModelImpl(
      id = "User",
      id2 = "User",
      `type` = Some("object"),
      description = Some("User login data"),
      name = Some("User"),
      properties = Map(
        "username" -> StringProperty(
          required = true,
          description = Some("User name"),
          enums = Set()
        ),
        "password" -> StringProperty(
          required = true,
          description = Some("User password"),
          enums = Set()
        ),
        "rememberMe" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description =
            Some("If set to `true` then a long-lived token will be created")
        )
      ),
      example =
        Some("""{"username": "user", "password": "pass", "rememberMe": true}""")
    )
  )

  val authTokenModel: Set[Model] = Set(
    ModelImpl(
      id = "AuthToken",
      id2 = "AuthToken",
      `type` = Some("object"),
      description = Some("JWT"),
      name = Some("AuthToken"),
      properties = Map(
        "token" -> StringProperty(
          required = true,
          description = Some("Token itself"),
          enums = Set()
        )
      ),
      example = Some(
        """{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"}"""
      )
    )
  )

  // Builds.
  val buildRequestModel: Set[Model] = Set(
    ModelImpl(
      id = "BuildRequest",
      id2 = "BuildRequest",
      `type` = Some("object"),
      description = Some("Build request"),
      name = Some("BuildRequest"),
      properties = Map(
        "name" -> StringProperty(
          required = true,
          description = Some("Name of build"),
          enums = Set()
        ),
        "shortName" -> StringProperty(
          required = true,
          description = Some("Short name of build. Used in URI path"),
          enums = Set()
        )
      ),
      example = Some("""{"name": "Building 1", "shortName": "b1"}""")
    )
  )

  val buildResponseModel: Set[Model] = Set(
    ModelImpl(
      id = "BuildResponse",
      id2 = "BuildResponse",
      `type` = Some("object"),
      description = Some("Build response"),
      name = Some("BuildResponse"),
      properties = Map(
        "name" -> StringProperty(
          required = true,
          description = Some("Name of build"),
          enums = Set()
        ),
        "shortName" -> StringProperty(
          required = true,
          description = Some("Short name of build. Used in URI path"),
          enums = Set()
        ),
        "floorsNumber" -> AbstractProperty(
          `type` = "integer",
          format = Some("int64"),
          required = true,
          description = Some("Number of floors in this build")
        ),
        "switchesNumber" -> AbstractProperty(
          `type` = "integer",
          format = Some("int64"),
          required = true,
          description = Some("Number of switches in this build")
        )
      ),
      example = Some(
        """{"name": "Building 1", "shortName": "b1"}, "floorsNumber": 1, "switchesNumber": 2}"""
      )
    )
  )

  // Floors.
  val floorRequestModel: Set[Model] = Set(
    ModelImpl(
      id = "FloorRequest",
      id2 = "FloorRequest",
      `type` = Some("object"),
      description = Some("Floor request"),
      name = Some("FloorRequest"),
      properties = Map(
        "number" -> AbstractProperty(
          `type` = "integer",
          format = Some("int32"),
          required = true,
          description = Some("Number of floor")
        ),
        "buildName" -> StringProperty(
          required = true,
          description = Some("Name of build of that floor"),
          enums = Set()
        ),
        "buildShortName" -> StringProperty(
          required = true,
          description = Some("Short name of build of that floor"),
          enums = Set()
        )
      ),
      example = Some(
        """{"number": 1, "buildName": "Building 1", "buildShortName": "b1"}"""
      )
    )
  )

  val floorResponseModel: Set[Model] = Set(
    ModelImpl(
      id = "FloorResponse",
      id2 = "FloorResponse",
      `type` = Some("object"),
      description = Some("Floor response"),
      name = Some("FloorResponse"),
      properties = Map(
        "number" -> AbstractProperty(
          `type` = "integer",
          format = Some("int32"),
          required = true,
          description = Some("Number of floor")
        ),
        "switchesNumber" -> AbstractProperty(
          `type` = "integer",
          format = Some("int64"),
          required = true,
          description = Some("Number of switches on that floor")
        )
      ),
      example = Some(
        """{"name": "Building 1", "shortName": "b1"}, "floorsNumber": 1, "switchesNumber": 2}"""
      )
    )
  )

  // Switches.
  val switchRequestModel: Set[Model] = Set(
    ModelImpl(
      id = "SwitchRequest",
      id2 = "SwitchRequest",
      `type` = Some("object"),
      description = Some("Switch request"),
      name = Some("SwitchRequest"),
      properties = Map(
        "retrieveFromNetData" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Retrieve data from NetData service")
        ),
        "retrieveIPFromDNS" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Retrieve IP from DNS")
        ),
        "retrieveUpLinkFromSeens" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Retrieve uplink from Seens service")
        ),
        "retrieveTechDataFromSNMP" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Retrieve technical data from via SNMP")
        ),
        "name" -> StringProperty(
          required = true,
          description = Some("Name of switch"),
          enums = Set()
        ),
        "ip" -> StringProperty(
          required = false,
          description = Some(
            "IP of switch. Required only if both `retrieveFromNetData` and `retrieveIPFromDNS` is set to `false`"
          ),
          enums = Set(),
          minLength = Some(7),
          maxLength = Some(15)
        ),
        "mac" -> StringProperty(
          required = false,
          description = Some(
            "MAC of switch. Required only if `retrieveFromNetData` is set to `false`"
          ),
          enums = Set(),
          minLength = Some(12),
          maxLength = Some(17)
        ),
        "snmpCommunity" -> StringProperty(
          required = false,
          description = Some(
            "SNMP community. Required only if `retrieveTechDataFromSNMP` is set to `true`"
          ),
          enums = Set()
        ),
        "revision" -> StringProperty(
          required = false,
          description = Some(
            "Revision of switch. Required only if `retrieveTechDataFromSNMP` is set to `false`"
          ),
          enums = Set()
        ),
        "serial" -> StringProperty(
          required = false,
          description = Some(
            "Serial number of switch. Required only if `retrieveTechDataFromSNMP` is set to `false`"
          ),
          enums = Set()
        ),
        "buildShortName" -> StringProperty(
          required = false,
          description = Some("Build of switch"),
          enums = Set()
        ),
        "floorNumber" -> AbstractProperty(
          `type` = "integer",
          format = Some("int32"),
          required = false,
          description = Some("Floor number of switch")
        ),
        "positionTop" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("Y-axis position of switch on plan")
        ),
        "positionTop" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("X-axis position of switch on plan")
        ),
        "upSwitchName" -> StringProperty(
          required = false,
          description = Some(
            "Name of upswitch. Required only if `retrieveUpLinkFromSeens` is set to `false`"
          ),
          enums = Set()
        ),
        "upLink" -> StringProperty(
          required = false,
          description = Some(
            "Port name of upswitch. Required only if `retrieveUpLinkFromSeens` is set to `false`"
          ),
          enums = Set()
        )
      ),
      example = Some(
        """{"retrieveFromNetData": false, "retrieveIPFromDNS": false, "retrieveUpLinkFromSeens": false, "retrieveTechDataFromSNMP": false, "name": "switch1", "ip": "192.168.1.1", "mac":"00:11:22:33:44:55", "snmpCommunity": "private", "revision": "Vendor Model", "serial": "AA000BB111", "buildShortName": "b1", "floorNumber": 1, "positionTop": 322.193, "positionLeft": 521.855, "upSwitchName": "switch2", "upLink": "48"}"""
      )
    )
  )

  val switchResponseModel: Set[Model] = Set(
    ModelImpl(
      id = "SwitchResponse",
      id2 = "SwitchResponse",
      `type` = Some("object"),
      description = Some("Switch response"),
      name = Some("SwitchResponse"),
      properties = Map(
        "name" -> StringProperty(
          required = true,
          description = Some("Name of switch"),
          enums = Set()
        ),
        "ip" -> StringProperty(
          required = true,
          description = Some("IP of switch"),
          enums = Set(),
          minLength = Some(7),
          maxLength = Some(15)
        ),
        "mac" -> StringProperty(
          required = true,
          description = Some("MAC of switch"),
          enums = Set(),
          minLength = Some(12),
          maxLength = Some(17)
        ),
        "revision" -> StringProperty(
          required = false,
          description = Some("Revision of switch"),
          enums = Set()
        ),
        "serial" -> StringProperty(
          required = false,
          description = Some("Serial number of switch"),
          enums = Set()
        ),
        "buildShortName" -> StringProperty(
          required = false,
          description = Some("Build of switch"),
          enums = Set()
        ),
        "floorNumber" -> AbstractProperty(
          `type` = "integer",
          format = Some("int32"),
          required = false,
          description = Some("Floor number of switch")
        ),
        "positionTop" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("Y-axis position of switch on plan")
        ),
        "positionLeft" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("X-axis position of switch on plan")
        ),
        "upSwitchName" -> StringProperty(
          required = false,
          description = Some("Name of upswitch"),
          enums = Set()
        ),
        "upLink" -> StringProperty(
          required = false,
          description = Some("Port name of upswitch"),
          enums = Set()
        )
      ),
      example = Some(
        """{"name": "switch1", "ip": "192.168.1.1", "mac":"00:11:22:33:44:55", "revision": "Vendor Model", "serial": "AA000BB111", "buildShortName": "b1", "floorNumber": 1, "positionTop": 322.193, "positionLeft": 521.855, "upSwitchName": "switch2", "upLink": "48"}"""
      )
    )
  )

  val switchResultModel: Set[Model] = Set(
    ModelImpl(
      id = "SwitchResult",
      id2 = "SwitchResult",
      `type` = Some("object"),
      description = Some("Result of adding/updating switch"),
      name = Some("SwitchResult"),
      properties = Map(
        "sw" -> AbstractProperty(
          `type` = "object",
          $ref = Some("#/definitions/SwitchResponse"),
          required = true,
          description = Some("Added/Updated switch")
        ),
        "seen" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Result of retrieving uplink from Seens service")
        ),
        "snmp" -> AbstractProperty(
          `type` = "boolean",
          required = true,
          description = Some("Result of retrieving technical data via SNMP")
        )
      ),
      example = Some(
        """{"sw": {"name": "switch1", "ip": "192.168.1.2", "mac":"66:11:22:33:44:FF", "revision": "Vendor Model A", "serial": "AA000BB123", "buildShortName": "b1", "floorNumber": 1, "positionTop": 322.193, "positionLeft": 521.855, "upSwitchName": "switch3", "upLink": "24"}, seen: true, snmp: true}"""
      )
    )
  )

  val savePositionRequestModel: Set[Model] = Set(
    ModelImpl(
      id = "SavePositionRequest",
      id2 = "SavePositionRequest",
      `type` = Some("object"),
      description = Some("Requet for updating position of switch on plan"),
      name = Some("SavePositionRequest"),
      properties = Map(
        "top" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("Y-axis position of switch on plan")
        ),
        "left" -> AbstractProperty(
          `type` = "number",
          format = Some("float"),
          required = false,
          description = Some("X-axis position of switch on plan")
        )
      ),
      example = Some(
        """{"top": 322.193, "left": 521.855}"""
      )
    )
  )
}
